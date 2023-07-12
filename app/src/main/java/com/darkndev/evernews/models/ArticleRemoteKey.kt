package com.darkndev.evernews.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_response_key")
data class ArticleRemoteKey(
    @PrimaryKey val category: String,
    val nextPageKey: Int
)