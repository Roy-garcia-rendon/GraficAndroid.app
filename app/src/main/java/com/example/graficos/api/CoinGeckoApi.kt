package com.example.graficos.api

import com.example.graficos.data.BitcoinPriceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("api/v3/coins/bitcoin/market_chart")
    suspend fun getBitcoinPrices(
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int = 1
    ): BitcoinPriceResponse

    @GET("api/v3/coins/ethereum/market_chart")
    suspend fun getEthereumPrices(
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int = 1
    ): BitcoinPriceResponse
} 