package com.example.graficos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.graficos.api.CoinGeckoApi
import com.example.graficos.data.*
import com.example.graficos.db.TradeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SimulatorViewModel(application: Application) : AndroidViewModel(application) {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CoinGeckoApi::class.java)

    private val db = TradeDatabase(application)

    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult = _backtestResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var lastRequestTime = 0L
    private val MIN_REQUEST_INTERVAL = 30000L // 30 segundos entre solicitudes

    fun runBacktest(symbol: String, interval: String) {
        viewModelScope.launch {
            try {
                // Verificar el tiempo desde la última solicitud
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
                    _error.value = "Por favor, espera 30 segundos entre simulaciones"
                    return@launch
                }

                _isLoading.value = true
                _error.value = null
                lastRequestTime = currentTime

                // Calcular rango de fechas (últimos 30 días)
                val endTime = Calendar.getInstance().timeInMillis / 1000
                val startTime = endTime - TimeUnit.DAYS.toSeconds(30)

                // Convertir símbolo al formato de CoinGecko
                val coinId = when (symbol) {
                    "BTCUSDT" -> "bitcoin"
                    "ETHUSDT" -> "ethereum"
                    else -> throw IllegalArgumentException("Símbolo no soportado")
                }

                // Obtener datos históricos con manejo de errores específico
                val response = try {
                    api.getHistoricalData(
                        id = coinId,
                        from = startTime,
                        to = endTime
                    )
                } catch (e: Exception) {
                    when {
                        e.message?.contains("429") == true -> {
                            _error.value = "Límite de solicitudes alcanzado. Por favor, espera unos minutos."
                            return@launch
                        }
                        e.message?.contains("Unable to resolve host") == true -> {
                            _error.value = "Error de conexión. Verifica tu conexión a internet."
                            return@launch
                        }
                        else -> throw e
                    }
                }

                // Convertir datos a velas
                val candles = response.prices.map { price ->
                    CandleData(
                        openTime = price[0].toLong(),
                        open = price[1],
                        high = price[1],
                        low = price[1],
                        close = price[1],
                        volume = 0.0
                    )
                }.sortedBy { it.openTime }

                if (candles.isEmpty()) {
                    _error.value = "No hay datos suficientes para el análisis"
                    return@launch
                }

                // Agrupar datos según el intervalo seleccionado
                val groupedCandles = when (interval) {
                    "1h" -> groupCandlesByHours(candles, 1)
                    "4h" -> groupCandlesByHours(candles, 4)
                    "1d" -> groupCandlesByHours(candles, 24)
                    else -> candles
                }

                // Ejecutar estrategia
                val trades = executeStrategy(groupedCandles)
                
                if (trades.isEmpty()) {
                    _error.value = "No se encontraron oportunidades de trading con los parámetros actuales"
                    return@launch
                }

                // Calcular resultados
                val totalProfit = trades.sumOf { it.profit }
                val winningTrades = trades.count { it.profit > 0 }
                val winRate = if (trades.isNotEmpty()) {
                    (winningTrades.toDouble() / trades.size) * 100
                } else {
                    0.0
                }

                val result = BacktestResult(
                    trades = trades,
                    totalProfit = totalProfit,
                    winRate = winRate,
                    numberOfTrades = trades.size
                )

                // Guardar resultados
                try {
                    trades.forEach { db.saveTrade(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _backtestResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = when {
                    e is IllegalArgumentException -> e.message
                    e.message?.contains("429") == true -> 
                        "Límite de solicitudes alcanzado. Por favor, espera unos minutos."
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Error de conexión. Verifica tu conexión a internet."
                    else -> "Error al ejecutar el backtest: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun groupCandlesByHours(candles: List<CandleData>, hours: Int): List<CandleData> {
        val millisInHour = TimeUnit.HOURS.toMillis(1)
        return candles.groupBy { 
            it.openTime / (millisInHour * hours) 
        }.map { (_, group) ->
            CandleData(
                openTime = group.first().openTime,
                open = group.first().open,
                high = group.maxOf { it.high },
                low = group.minOf { it.low },
                close = group.last().close,
                volume = group.sumOf { it.volume }
            )
        }
    }

    private fun executeStrategy(candles: List<CandleData>): List<TradeResult> {
        val trades = mutableListOf<TradeResult>()
        var inPosition = false
        var entryPrice = 0.0
        val stopLoss = 0.05 // 5%
        val takeProfit = 0.05 // 5%

        candles.forEachIndexed { index, candle ->
            if (!inPosition) {
                // Calcular el máximo de las últimas 10 velas o menos
                val lookback = minOf(10, index)
                val recentCandles = candles.subList(maxOf(0, index - lookback), index + 1)
                val recentHigh = recentCandles.maxOfOrNull { it.high } ?: return@forEachIndexed

                if (candle.close < recentHigh * (1 - stopLoss)) {
                    // Comprar
                    entryPrice = candle.close
                    trades.add(
                        TradeResult(
                            type = "BUY",
                            price = entryPrice,
                            amount = 1.0,
                            date = candle.openTime
                        )
                    )
                    inPosition = true
                }
            } else {
                // Verificar condiciones de salida
                val profitPercent = (candle.close - entryPrice) / entryPrice
                
                if (profitPercent <= -stopLoss || profitPercent >= takeProfit) {
                    // Vender
                    val profit = (candle.close - entryPrice) * 1.0
                    trades.add(
                        TradeResult(
                            type = "SELL",
                            price = candle.close,
                            amount = 1.0,
                            date = candle.openTime,
                            profit = profit
                        )
                    )
                    inPosition = false
                }
            }
        }

        return trades
    }

    fun clearError() {
        _error.value = null
    }
} 