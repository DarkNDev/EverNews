package com.darkndev.evernews.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.darkndev.evernews.database.ArticleDao
import com.darkndev.evernews.database.ArticleRemoteKeyDao
import com.darkndev.evernews.database.BookmarkedArticleDao
import com.darkndev.evernews.database.Converters
import com.darkndev.evernews.models.Article
import com.darkndev.evernews.models.ArticleRemoteKey
import com.darkndev.evernews.models.BookmarkedArticle

@Database(
    entities = [Article::class, BookmarkedArticle::class, ArticleRemoteKey::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EverNewsDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    abstract fun articleRemoteKeyDao(): ArticleRemoteKeyDao

    abstract fun bookmarkDao():BookmarkedArticleDao
}