package com.darkndev.evernews.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.darkndev.evernews.models.BookmarkedArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkedArticleDao {

    @Upsert
    suspend fun bookmarkArticle(article: BookmarkedArticle)

    @Query("DELETE FROM bookmark_table WHERE url=:url")
    suspend fun unBookmarkArticle(url: String)

    @Query("SELECT * FROM bookmark_table")
    fun getBookmarkedArticles(): Flow<List<BookmarkedArticle>>

    @Query("DELETE FROM bookmark_table")
    suspend fun unBookmarkAllArticles()

    @Query("SELECT count(*)!=0 FROM bookmark_table WHERE url=:url")
    fun isBookmarked(url: String): Flow<Boolean>
}