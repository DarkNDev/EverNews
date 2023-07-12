package com.darkndev.evernews.data

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.darkndev.evernews.api.NewsApi
import com.darkndev.evernews.models.Article
import com.darkndev.evernews.models.ArticleRemoteKey
import com.darkndev.evernews.utils.toSearchArticles
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE = 1

class EverNewsRemoteMediator(
    private val query: String,
    private val newsApi: NewsApi,
    private val database: EverNewsDatabase,
    private val refreshOnInit: Boolean
) : RemoteMediator<Int, Article>() {

    private val articleDao = database.articleDao()
    private val articleRemoteKeyDao = database.articleRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Article>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> STARTING_PAGE
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> articleRemoteKeyDao.getRemoteKey(query).nextPageKey
        }

        try {

            val response = newsApi.getSearchNews(
                query = query,
                language = "en",
                sortBy = "publishedAt",
                pageNumber = page,
                pageSize = state.config.pageSize
            )
            val articles = response.articles

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.deleteArticles(query)
                }

                val lastPosition = articleDao.getPagingLastArticlePosition(query) ?: 0
                var typePosition = lastPosition + 1

                val mappedArticleEntities = articles.toSearchArticles(
                    query = {
                        query
                    },
                    position = {
                        typePosition++
                    }
                )

                val nextPageKey = page + 1

                articleDao.upsertArticles(mappedArticleEntities)
                articleRemoteKeyDao.insertRemoteKey(ArticleRemoteKey(query, nextPageKey))
            }
            //println("TAG: Success")
            return MediatorResult.Success(endOfPaginationReached = articles.isEmpty())
        } catch (exception: IOException) {
            //println("TAG: IO: ${exception.localizedMessage}")
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            //println("TAG: HttpExp: ${exception.localizedMessage}")
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return if (refreshOnInit) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}