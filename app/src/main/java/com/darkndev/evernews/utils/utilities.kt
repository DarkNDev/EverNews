package com.darkndev.evernews.utils

import android.view.View
import androidx.appcompat.widget.SearchView
import com.darkndev.evernews.api.NewsResponse
import com.darkndev.evernews.models.Article
import com.darkndev.evernews.models.BookmarkedArticle

inline fun List<NewsResponse.ArticleDto>.toTopArticles(
    category: () -> String
) = this.mapIndexed { position, articleDto ->
    Article(
        category = category(),
        source = Article.Source(articleDto.source.name),
        author = articleDto.author ?: "",
        description = articleDto.description ?: "",
        publishedAt = articleDto.publishedAt ?: "",
        title = articleDto.title ?: "",
        url = articleDto.url,
        urlToImage = articleDto.urlToImage ?: "",
        position = position + 1
    )
}

inline fun List<NewsResponse.ArticleDto>.toSearchArticles(
    query: () -> String,
    position: () -> Int
) = this.map { articleDto ->
    Article(
        category = query(),
        source = Article.Source(articleDto.source.name),
        author = articleDto.author ?: "",
        description = articleDto.description ?: "",
        publishedAt = articleDto.publishedAt ?: "",
        title = articleDto.title ?: "",
        url = articleDto.url,
        urlToImage = articleDto.urlToImage ?: "",
        position = position()
    )
}

fun Article.toBookmarkArticle() = BookmarkedArticle(
    title, url, urlToImage
)

inline fun errorMessage(throwable: () -> Throwable) =
    "Could not load News: ${throwable().localizedMessage ?: "An unknown error occurred"}"

inline fun SearchView.onQueryTextChange(crossinline listener: (String?) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText)
            return true
        }
    })
}

inline fun SearchView.onQueryTextSubmit(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!query.isNullOrBlank()) {
                listener(query)
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    })
}

inline fun <T : View> T.showIfOrInvisible(condition: (T) -> Boolean) {
    if (condition(this)) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}