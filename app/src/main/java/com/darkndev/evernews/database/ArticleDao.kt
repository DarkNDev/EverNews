package com.darkndev.evernews.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.darkndev.evernews.models.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    //top

    @Upsert
    suspend fun upsertArticles(articlesList: List<Article>)

    @Query("DELETE FROM article_table WHERE category=:category")
    suspend fun deleteArticles(category: String)

    @Query("SELECT * FROM article_table WHERE category=:category")
    fun getFlowArticles(category: String): Flow<List<Article>>

    //search and paging

    @Query("SELECT * FROM article_table WHERE category=:category ORDER BY position")
    fun getPagingArticles(category: String): PagingSource<Int, Article>

    @Query("SELECT MAX(position) FROM article_table WHERE category=:category")
    suspend fun getPagingLastArticlePosition(category: String): Int?
}