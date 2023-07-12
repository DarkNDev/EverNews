package com.darkndev.evernews.api

data class NewsResponse(
    val articles: List<ArticleDto>
) {
    data class ArticleDto(
        val author: String?,
        val source: SourceDto,
        val description: String?,
        val publishedAt: String?,
        val title: String?,
        val url: String,
        val urlToImage: String?
    ) {
        data class SourceDto(
            val name: String
        )
    }
}