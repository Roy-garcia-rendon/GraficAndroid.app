package com.example.graficos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.graficos.data.BitcoinPrice
import com.example.graficos.ui.theme.GraficosTheme
import com.example.graficos.viewmodel.BitcoinViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.graficos.components.BitcoinChart

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraficosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BitcoinPriceScreen()
                }
            }
        }
    }
}

@Composable
fun BitcoinPriceScreen(
    viewModel: BitcoinViewModel = viewModel()
) {
    val bitcoinPrices by viewModel.bitcoinPrices.collectAsState()
    val ethereumPrices by viewModel.ethereumPrices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Cryptocurrency Charts",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Bitcoin Section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CryptoSection(
                            title = "Bitcoin",
                            prices = bitcoinPrices,
                            color = Color(0xFFF7931A)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ethereum Section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CryptoSection(
                            title = "Ethereum",
                            prices = ethereumPrices,
                            color = Color(0xFF627EEA)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoSection(
    title: String,
    prices: List<BitcoinPrice>,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "$title Price Chart",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            BitcoinChart(
                prices = prices,
                modifier = Modifier.fillMaxSize(),
                lineColor = color
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prices) { price ->
                PriceCard(
                    price = price,
                    cryptoName = title,
                    accentColor = color
                )
            }
        }
    }
}

@Composable
fun PriceCard(
    price: BitcoinPrice,
    cryptoName: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E),
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cryptoName,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = formatTimestamp(price.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                text = "$${String.format("%.2f", price.price)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}