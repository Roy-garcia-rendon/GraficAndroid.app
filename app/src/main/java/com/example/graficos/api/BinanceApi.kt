package com.example.graficos.api

import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {
    @GET("api/v3/klines")
    suspend fun getHistoricalPrices(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): List<List<Any>>
} 