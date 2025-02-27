package com.example.graficos.data

data class CandleData(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class TradeResult(
    val type: String, // BUY or SELL
    val price: Double,
    val amount: Double,
    val date: Long,
    val profit: Double = 0.0
)

data class BacktestResult(
    val trades: List<TradeResult>,
    val totalProfit: Double,
    val winRate: Double,
    val numberOfTrades: Int
) 