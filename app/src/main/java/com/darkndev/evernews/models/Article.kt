package com.darkndev.evernews.models

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(tableName = "article_table", primaryKeys = ["category", "url"])
@Parcelize
data class Article(
    val category: String, //query category
    val author: String?,
    val source: Source,
    val description: String?,
    val publishedAt: String?,
    val title: String?,
    val url: String,
    val urlToImage: String?,
    val updatedAt: Long = System.currentTimeMillis(), //created
    val position: Int //position
) : Parcelable {

    @Parcelize
    data class Source(
        val name: String
    ) : Parcelable
}