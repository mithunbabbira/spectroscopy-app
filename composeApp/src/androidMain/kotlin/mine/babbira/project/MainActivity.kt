package mine.babbira.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import android.widget.Toast
import android.bluetooth.BluetoothDevice
import android.util.Log

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private var bluetoothConnection: BluetoothConnection? = null
    private val limitH = 100.0
    private val limitM = 80.0
    private val limitL = 40.0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var text by remember { mutableStateOf("") }
            var chartValues by remember { mutableStateOf<List<Double>>(emptyList()) }
            val bluetoothManager = BluetoothManager(this)
            val device = bluetoothManager.getPairedDevices()?.find { it.name == "ESP32_Spectral" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (device != null) {
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            bluetoothConnection?.close()
                            bluetoothConnection = bluetoothManager.connectToDevice(device)
                            
                            if (bluetoothConnection?.connect() == true) {
                                while (isActive) {
                                    val rawData = bluetoothConnection?.receiveData()
                                    if (!rawData.isNullOrEmpty()) {
                                        bluetoothConnection?.processData(rawData)?.let { data ->
                                            withContext(Dispatchers.Main) {
                                                text = data.values.joinToString(",")
                                                val values = text.split(",").map { it.toDouble() }
                                                chartValues = values
                                                handleSpectralData(values)
                                            }
                                        }
                                    }
                                    delay(100)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) {
                        Text("Receive Data")
                    }
                    
                    if (chartValues.isNotEmpty()) {
                        SpectralChart(
                            values = chartValues,
                            limitH = limitH,
                            limitM = limitM,
                            limitL = limitL
                        )
                    }
                    
                    Text(text)
                } else {
                    Text("No ESP32 device found")
                }
            }
        }
    }

    private fun handleSpectralData(values: List<Double>) {
        Log.d("SpectralData", "Received values: $values")
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnection?.close()
    }
}

@Composable
fun SpectralChart(
    values: List<Double>,
    limitH: Double,
    limitM: Double,
    limitL: Double
) {
    val maxValue = values.maxOrNull() ?: 0.0
    val minValue = values.minOrNull() ?: 0.0
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val xStep = width / (values.size - 1)
        val yScale = height / (maxValue - minValue)

        // Draw axes
        drawLine(
            Color.Black,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f
        )
        drawLine(
            Color.Black,
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 2f
        )

        // Draw the line graph
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * xStep
            val y = height - ((value - minValue) * yScale).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw points
            drawCircle(
                color = when {
                    value > limitH -> Color.Red
                    value > limitM -> Color(0xFFFF4500) // OrangeRed
                    value < limitL -> Color(0xFF3CB371) // MediumSeaGreen
                    else -> Color.Black
                },
                radius = 8f,
                center = Offset(x, y)
            )
        }

        // Draw the path
        drawPath(
            path,
            Color.Black,
            style = Stroke(width = 2f)
        )
    }
}

