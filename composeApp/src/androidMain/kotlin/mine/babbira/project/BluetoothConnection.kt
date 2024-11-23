package mine.babbira.project

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*
import android.util.Log

data class SpectralData(
    val values: List<Double>,
    val timestamp: Long = System.currentTimeMillis()
)

class BluetoothConnection(device: BluetoothDevice) {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null
    private var isConnected = false
    private var lastDataReceived = 0L
    private val TIMEOUT_MS = 5000L  // 5 seconds timeout

    init {
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Socket creation failed", e)
        }
    }

    fun connect(): Boolean {
        return try {
            if (isConnected) {
                Log.d("BluetoothConnection", "Already connected")
                return true
            }
            Log.d("BluetoothConnection", "Attempting to connect")
            socket?.connect()
            isConnected = true
            Log.d("BluetoothConnection", "Connection successful")
            true
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Connection failed", e)
            isConnected = false
            false
        }
    }

    fun receiveData(): String {
        if (!isConnected) {
            Log.e("BluetoothConnection", "Not connected")
            return ""
        }
        
        return try {
            val inputStream = socket?.inputStream
            val buffer = ByteArray(1024)
            val bytes = inputStream?.read(buffer)
            
            if (bytes != null && bytes > 0) {
                val data = String(buffer, 0, bytes)
                if (data.isNotEmpty() && data.contains(",")) {  // Validate data format
                    Log.d("BluetoothConnection", "Data received: $data")
                    data
                } else {
                    Log.d("BluetoothConnection", "Empty or invalid data received")
                    ""
                }
            } else {
                ""
            }
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Failed to receive data", e)
            isConnected = false
            ""
        }
    }

    fun close() {
        try {
            socket?.close()
            isConnected = false
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Error closing socket", e)
        }
    }

    fun processData(rawData: String): SpectralData? {
        return try {
            val values = rawData.trim().split(",").map { it.toDouble() }
            if (values.size == 18) {  // Validate expected data format
                SpectralData(values)
            } else {
                Log.w("BluetoothConnection", "Invalid data format: expected 18 values, got ${values.size}")
                null
            }
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Error processing data", e)
            null
        }
    }

    private fun checkConnection() {
        val now = System.currentTimeMillis()
        if (isConnected && (now - lastDataReceived) > TIMEOUT_MS) {
            Log.w("BluetoothConnection", "No data received for ${TIMEOUT_MS}ms, reconnecting...")
            close()
            connect()
        }
    }
} 