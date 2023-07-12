package com.darkndev.evernews.api

import com.darkndev.evernews.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NewsApi {

    companion object {
        const val BASE_URL = "https://newsapi.org/"
        const val NEWS_API_KEY = BuildConfig.NEWS_API_KEY
    }

    @Headers("X-Api-Key: $NEWS_API_KEY")
    @GET("v2/top-headlines")
    suspend fun getTopNews(
        @Query("country")
        countryCode: String,
        @Query("category")
        category: String,
        @Query("page")
        pageNumber: Int,
        @Query("pageSize")
        pageSize: Int
    ): NewsResponse

    @Headers("X-Api-Key: $NEWS_API_KEY")
    @GET("v2/everything")
    suspend fun getSearchNews(
        @Query("q")
        query: String,
        @Query("language")
        language: String,
        @Query("sortBy")
        sortBy: String,
        @Query("page")
        pageNumber: Int,
        @Query("pageSize")
        pageSize: Int
    ): NewsResponse
}