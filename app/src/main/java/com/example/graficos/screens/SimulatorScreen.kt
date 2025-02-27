package com.example.graficos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.graficos.data.BacktestResult
import com.example.graficos.data.TradeResult
import com.example.graficos.viewmodel.SimulatorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimulatorScreen(
    viewModel: SimulatorViewModel,
    onBack: () -> Unit
) {
    var selectedCrypto by remember { mutableStateOf("BTCUSDT") }
    var selectedInterval by remember { mutableStateOf("1h") }
    var isRunning by remember { mutableStateOf(false) }
    
    val backtestResult by viewModel.backtestResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Simulador de Trading",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Button(onClick = onBack) {
                Text("Volver")
            }
        }

        // Configuración
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Selector de Criptomoneda
                Text("Par de Trading", color = Color.White)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("BTCUSDT", "ETHUSDT").forEach { pair ->
                        FilterChip(
                            selected = selectedCrypto == pair,
                            onClick = { selectedCrypto = pair },
                            label = { Text(pair) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de Intervalo
                Text("Intervalo", color = Color.White)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1h", "4h", "1d").forEach { interval ->
                        FilterChip(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval },
                            label = { Text(interval) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de inicio
                Button(
                    onClick = {
                        isRunning = true
                        viewModel.runBacktest(selectedCrypto, selectedInterval)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Ejecutando..." else "Iniciar Simulación")
                }
            }
        }

        // Resultados
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            backtestResult != null -> {
                BacktestResultView(backtestResult!!)
            }
        }
    }
}

@Composable
fun BacktestResultView(result: BacktestResult) {
    Column {
        // Resumen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Resumen del Backtest",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Beneficio Total: $${String.format("%.2f", result.totalProfit)}",
                    color = if (result.totalProfit >= 0) Color.Green else Color.Red
                )
                Text(
                    "Tasa de Éxito: ${String.format("%.1f", result.winRate)}%",
                    color = Color.White
                )
                Text(
                    "Número de Operaciones: ${result.numberOfTrades}",
                    color = Color.White
                )
            }
        }

        // Lista de operaciones
        Text(
            "Historial de Operaciones",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(result.trades) { trade ->
                TradeCard(trade)
            }
        }
    }
}

@Composable
fun TradeCard(trade: TradeResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    trade.type,
                    color = if (trade.type == "BUY") Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Precio: $${String.format("%.2f", trade.price)}",
                    color = Color.White
                )
                Text(
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(trade.date)),
                    color = Color.Gray
                )
            }
            if (trade.profit != 0.0) {
                Text(
                    "$${String.format("%.2f", trade.profit)}",
                    color = if (trade.profit > 0) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 