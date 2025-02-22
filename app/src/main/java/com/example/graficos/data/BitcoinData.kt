package com.example.graficos.data

data class BitcoinPriceResponse(
    val prices: List<List<Double>>  // [timestamp, price]
)

data class BitcoinPrice(
    val timestamp: Long,
    val price: Double
) 