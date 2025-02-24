package com.example.graficos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.graficos.viewmodel.BitcoinViewModel
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun ConversorScreen(
    viewModel: BitcoinViewModel,
    onBack: () -> Unit
) {
    var selectedCrypto by remember { mutableStateOf("Bitcoin") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0.0) }

    val bitcoinPrice by viewModel.bitcoinPrices.collectAsState()
    val ethereumPrice by viewModel.ethereumPrices.collectAsState()

    // Obtener el precio actual de la criptomoneda seleccionada
    val currentCryptoPrice = when (selectedCrypto) {
        "Bitcoin" -> bitcoinPrice.lastOrNull()?.price ?: 0.0
        "Ethereum" -> ethereumPrice.lastOrNull()?.price ?: 0.0
        else -> 0.0
    }

    // Tasas de conversión (aproximadas)
    val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "MXN" to 17.0
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Conversor de Criptomonedas",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Selector de Criptomoneda
        Text("Selecciona la Criptomoneda", color = Color.White)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Bitcoin", "Ethereum").forEach { crypto ->
                FilterChip(
                    selected = selectedCrypto == crypto,
                    onClick = { selectedCrypto = crypto },
                    label = { Text(crypto) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Moneda
        Text("Selecciona la Moneda", color = Color.White)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("USD", "EUR", "MXN").forEach { currency ->
                FilterChip(
                    selected = selectedCurrency == currency,
                    onClick = { selectedCurrency = currency },
                    label = { Text(currency) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de entrada
        OutlinedTextField(
            value = amount,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    amount = it
                    // Calcular conversión
                    result = try {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        val rate = exchangeRates[selectedCurrency] ?: 1.0
                        amountValue * currentCryptoPrice * rate
                    } catch (e: Exception) {
                        0.0
                    }
                }
            },
            label = { Text("Cantidad de ${selectedCrypto}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Resultado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Resultado",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    String.format("%.2f %s", result, selectedCurrency),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de regreso
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver")
        }
    }
} 