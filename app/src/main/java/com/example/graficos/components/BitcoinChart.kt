package com.example.graficos.components

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.graficos.data.BitcoinPrice
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor

@Composable
fun BitcoinChart(
    prices: List<BitcoinPrice>,
    modifier: Modifier = Modifier,
    lineColor: ComposeColor = ComposeColor(0xFF1E88E5)
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
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(true)
                    gridColor = AndroidColor.parseColor("#333333")
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return sdf.format(value.toLong())
                        }
                    }
                }
                
                // Configuración del eje Y izquierdo
                axisLeft.apply {
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(true)
                    gridColor = AndroidColor.parseColor("#333333")
                }
                
                // Desactivar eje Y derecho
                axisRight.isEnabled = false
                
                // Configuración de la leyenda
                legend.apply {
                    textColor = AndroidColor.WHITE
                    textSize = 14f
                }
                
                // Color de fondo
                setBackgroundColor(AndroidColor.BLACK)
            }
        },
        update = { chart ->
            val entries = prices.map { price ->
                Entry(price.timestamp.toFloat(), price.price.toFloat())
            }
            
            val dataSet = LineDataSet(entries, "Price").apply {
                color = lineColor.toArgb()
                setDrawCircles(false)
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = lineColor.toArgb()
                fillAlpha = 50
                valueTextColor = AndroidColor.WHITE
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
} 