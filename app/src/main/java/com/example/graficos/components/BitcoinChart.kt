package com.example.graficos.components

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.graficos.data.BitcoinPrice
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BitcoinChart(
    prices: List<BitcoinPrice>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
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
                    textColor = Color.WHITE
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#333333") // Líneas de la cuadrícula más oscuras
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return sdf.format(value.toLong())
                        }
                    }
                }
                
                // Configuración del eje Y izquierdo
                axisLeft.apply {
                    textColor = Color.WHITE
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#333333") // Líneas de la cuadrícula más oscuras
                }
                
                // Desactivar eje Y derecho
                axisRight.isEnabled = false
                
                // Configuración de la leyenda
                legend.apply {
                    textColor = Color.WHITE
                    textSize = 14f
                }
                
                // Color de fondo
                setBackgroundColor(Color.BLACK)
            }
        },
        update = { chart ->
            // Crear entradas para el gráfico
            val entries = prices.map { price ->
                Entry(price.timestamp.toFloat(), price.price.toFloat())
            }
            
            // Crear y configurar el conjunto de datos
            val dataSet = LineDataSet(entries, "Bitcoin Price").apply {
                color = Color.rgb(30, 136, 229)      // Línea azul
                setDrawCircles(false)
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = Color.rgb(30, 136, 229)  // Relleno azul
                fillAlpha = 50                       // Más opacidad en el relleno
                valueTextColor = Color.WHITE
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER // Línea más suave
            }
            
            // Actualizar datos del gráfico
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
} 