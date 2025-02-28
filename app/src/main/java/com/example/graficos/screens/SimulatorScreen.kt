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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import android.graphics.Color as AndroidColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState

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
            
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(0xFF1E1E1E),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Error en la Simulación",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = error ?: "Error desconocido",
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            
                            if (error?.contains("espera 30 segundos") == true) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    color = Color.White.copy(alpha = 0.5f),
                                    strokeWidth = 4.dp
                                )
                                CountdownTimer(
                                    initialTime = 30,
                                    onFinish = {
                                        viewModel.clearError()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            backtestResult != null -> {
                BacktestResultView(backtestResult!!)
            }
        }
    }
}

@Composable
fun BacktestResultView(result: BacktestResult) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Resumen
        item {
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
        }

        // Gráfica
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                BacktestChart(trades = result.trades)
            }
        }

        // Título del historial
        item {
            Text(
                "Historial de Operaciones",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Lista de operaciones
        items(result.trades) { trade ->
            TradeCard(trade)
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BacktestChart(trades: List<TradeResult>) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Configuración del gráfico
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                
                // Configuración del eje X
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(true)
                    gridColor = AndroidColor.parseColor("#333333")
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        private val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return sdf.format(value.toLong())
                        }
                    }
                }
                
                // Configuración del eje Y
                axisLeft.apply {
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(true)
                    gridColor = AndroidColor.parseColor("#333333")
                }
                
                axisRight.isEnabled = false
                
                // Configuración de la leyenda
                legend.apply {
                    textColor = AndroidColor.WHITE
                    textSize = 12f
                }
                
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
        update = { chart ->
            // Calcular balance acumulado
            var balance = 0.0
            val entries = mutableListOf<Entry>()
            
            trades.forEach { trade ->
                if (trade.type == "SELL") {
                    balance += trade.profit
                    entries.add(Entry(trade.date.toFloat(), balance.toFloat()))
                }
            }
            
            val dataSet = LineDataSet(entries, "Balance").apply {
                color = AndroidColor.parseColor("#1E88E5")
                setDrawCircles(true)
                circleRadius = 4f
                circleColors = listOf(AndroidColor.parseColor("#1E88E5"))
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = AndroidColor.parseColor("#1E88E5")
                fillAlpha = 50
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 10f
                mode = LineDataSet.Mode.LINEAR
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
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

@Composable
fun CountdownTimer(
    initialTime: Int,
    onFinish: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(initialTime) }
    
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            onFinish()
        }
    }

    Text(
        text = "$timeLeft segundos",
        color = Color.White.copy(alpha = 0.8f),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
} 