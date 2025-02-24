package com.example.graficos.api

import com.example.graficos.data.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoNewsApi {
    @GET("v2/news/")
    suspend fun getNews(
        @Query("lang") lang: String = "EN",
        @Query("sortOrder") sortOrder: String = "popular"
    ): NewsResponse
} 