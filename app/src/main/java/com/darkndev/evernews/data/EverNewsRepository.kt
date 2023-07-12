package com.darkndev.evernews.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.withTransaction
import com.darkndev.evernews.api.NewsApi
import com.darkndev.evernews.utils.networkBoundResource
import com.darkndev.evernews.utils.toTopArticles
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EverNewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val database: EverNewsDatabase
) {

    private val articleDao = database.articleDao()

    val bookmarkDao = database.bookmarkDao()

    fun getBookmarkedItems() = bookmarkDao.getBookmarkedArticles()

    fun getSearchNews(
        query: String,
        refreshOnInit: Boolean
    ) = Pager(
        config = PagingConfig(pageSize = 100, maxSize = 300),
        remoteMediator = EverNewsRemoteMediator(query, newsApi, database, refreshOnInit),
        pagingSourceFactory = { articleDao.getPagingArticles(query) }
    ).flow

    fun getTopNews(
        category: String,
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ) = networkBoundResource(
        query = {
            //println("TAG: FETCHING FROM ROOM")
            articleDao.getFlowArticles(category)
        },
        fetch = {
            //println("TAG: FETCHING FROM INTERNET")
            newsApi.getTopNews(
                "in",
                category,
                1,
                100
            ).articles
        },
        saveFetchResult = { articleDtoList ->
            //println("TAG: SAVING FETCHED RESULT TO ROOM")
            database.withTransaction {
                articleDao.deleteArticles(category)
                articleDao.upsertArticles(articleDtoList.toTopArticles { category })
            }
        },
        shouldFetch = { cachedTopArticleList ->
            //println("TAG: CHECKING IF FETCHING FROM INTERNET IS NEEDED")
            if (forceRefresh) {
                true
            } else {
                val sortedArticles = cachedTopArticleList.sortedBy { article ->
                    article.updatedAt
                }
                val oldestTimestamp = sortedArticles.firstOrNull()?.updatedAt
                val needsRefresh = oldestTimestamp == null ||
                        oldestTimestamp < System.currentTimeMillis() -
                        TimeUnit.MINUTES.toMillis(60)
                needsRefresh
            }
        },
        onFetchSuccess = onFetchSuccess,
        onFetchFailed = { throwable ->
            //println("TAG: FETCHING FROM INTERNET FAILED")
            if (throwable !is HttpException && throwable !is IOException) {
                throw throwable
            }
            onFetchFailed(throwable)
        }
    )
}