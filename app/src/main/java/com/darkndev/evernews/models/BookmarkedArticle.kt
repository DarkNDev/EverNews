package com.darkndev.evernews.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "bookmark_table")
@Parcelize
data class BookmarkedArticle(
    val title: String?,
    @PrimaryKey(autoGenerate = false)
    val url: String,
    val urlToImage: String?
) : Parcelable