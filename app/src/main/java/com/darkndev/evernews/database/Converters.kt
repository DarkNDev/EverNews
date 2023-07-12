package com.darkndev.evernews.database

import androidx.room.TypeConverter
import com.darkndev.evernews.models.Article

class Converters {

    @TypeConverter
    fun toSource(name: String): Article.Source {
        return Article.Source(name)
    }

    @TypeConverter
    fun fromSource(source: Article.Source): String {
        return source.name
    }
}