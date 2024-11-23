package mine.babbira.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import android.widget.Toast
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {
    private var bluetoothConnection: BluetoothConnection? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var text by remember { mutableStateOf("") }
            val bluetoothManager = BluetoothManager(this)
            val device = bluetoothManager.getPairedDevices()?.find { it.name == "ESP32_Spectral" }

            Column {
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
                                            }
                                        }
                                    }
                                    delay(100) // Add a small delay between reads
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
                    Text(text)
                } else {
                    Text("No ESP32 device found")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnection?.close()
    }
}

