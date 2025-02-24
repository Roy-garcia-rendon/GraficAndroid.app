package com.example.graficos.data

data class NewsResponse(
    val Data: List<NewsArticle>
)

data class NewsArticle(
    val id: String,
    val title: String,
    val body: String,
    val categories: String,
    val url: String,
    val imageurl: String,
    val source: String,
    val published_on: Long
) 