package com.darkndev.evernews.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.darkndev.evernews.models.ArticleRemoteKey

@Dao
interface ArticleRemoteKeyDao {

    @Upsert
    suspend fun insertRemoteKey(remoteKey: ArticleRemoteKey)

    @Query("SELECT * FROM news_response_key WHERE category=:category")
    suspend fun getRemoteKey(category: String): ArticleRemoteKey
}