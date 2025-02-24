package com.example.graficos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.graficos.api.CoinGeckoApi
import com.example.graficos.data.BitcoinPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.graficos.api.CryptoNewsApi
import com.example.graficos.data.NewsArticle

class BitcoinViewModel : ViewModel() {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CoinGeckoApi::class.java)

    private val newsApi = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/data/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CryptoNewsApi::class.java)

    private val _bitcoinPrices = MutableStateFlow<List<BitcoinPrice>>(emptyList())
    val bitcoinPrices = _bitcoinPrices.asStateFlow()

    private val _ethereumPrices = MutableStateFlow<List<BitcoinPrice>>(emptyList())
    val ethereumPrices = _ethereumPrices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _news = MutableStateFlow<List<NewsArticle>>(emptyList())
    val news = _news.asStateFlow()

    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews = _isLoadingNews.asStateFlow()

    private val _errorNews = MutableStateFlow<String?>(null)
    val errorNews = _errorNews.asStateFlow()

    init {
        loadPrices()
        loadNews()
    }

    fun loadPrices() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Cargar Bitcoin
                val bitcoinResponse = api.getBitcoinPrices()
                _bitcoinPrices.value = bitcoinResponse.prices.map { 
                    BitcoinPrice(
                        timestamp = it[0].toLong(),
                        price = it[1]
                    )
                }

                // Cargar Ethereum
                val ethereumResponse = api.getEthereumPrices()
                _ethereumPrices.value = ethereumResponse.prices.map { 
                    BitcoinPrice(
                        timestamp = it[0].toLong(),
                        price = it[1]
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoadingNews.value = true
                _errorNews.value = null
                val response = newsApi.getNews()
                _news.value = response.Data
            } catch (e: Exception) {
                _errorNews.value = e.message
            } finally {
                _isLoadingNews.value = false
            }
        }
    }
} 