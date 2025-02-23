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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

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
    
    // Estado para controlar el menú desplegable
    var showMenu by remember { mutableStateOf(false) }
    // Estado para controlar qué criptomoneda mostrar
    var selectedCrypto by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Barra superior con título y menú
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BitTrend",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            // Botón del menú con ícono de tres puntos
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Menú desplegable con fondo oscuro
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E))
                        .padding(vertical = 8.dp)
                ) {
                    // Opción BitTrend (muestra ambas)
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "BitTrend",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        },
                        onClick = { 
                            selectedCrypto = "ALL"
                            showMenu = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )
                    
                    // Opción Bitcoin
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Bitcoin",
                                color = Color.White
                            )
                        },
                        onClick = { 
                            selectedCrypto = "BTC"
                            showMenu = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )
                    
                    // Opción Ethereum
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Ethereum",
                                color = Color.White
                            )
                        },
                        onClick = { 
                            selectedCrypto = "ETH"
                            showMenu = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )

                    Divider(
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Opción GitHub
                    val context = LocalContext.current
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "GitHub",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "GitHub",
                                    color = Color.White
                                )
                            }
                        },
                        onClick = { 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Roy-garcia-rendon"))
                            context.startActivity(intent)
                            showMenu = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )
                }
            }
        }

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
                    when (selectedCrypto) {
                        "ALL" -> {
                            // Mostrar ambas criptomonedas
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
                        "BTC" -> {
                            // Mostrar solo Bitcoin
                            CryptoSection(
                                title = "Bitcoin",
                                prices = bitcoinPrices,
                                color = Color(0xFFF7931A)
                            )
                        }
                        "ETH" -> {
                            // Mostrar solo Ethereum
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