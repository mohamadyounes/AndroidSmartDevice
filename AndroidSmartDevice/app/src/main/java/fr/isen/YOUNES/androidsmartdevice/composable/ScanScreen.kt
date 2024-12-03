package fr.isen.YOUNES.androidsmartdevice.composable

// Import necessary Android and Jetpack Compose libraries
import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.bluetooth.le.ScanCallback
import android.util.Log



// Data class representing a BLE device
data class BleDevice(
    val name: String, // Device name
    val address: String, // Device MAC address
    val rssi: Int // Signal strength
)

// Composable function for the BLE scanning screen
// ScanScreen Composable
@Composable
fun ScanScreen(navController: NavController) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    var isScanning by remember { mutableStateOf(false) }
    val devices = remember { mutableStateListOf<BleDevice>() }
    val uniqueDevices = remember { mutableStateOf(mutableSetOf<String>()) }
    var scanCallback by remember { mutableStateOf<ScanCallback?>(null) }

    // Function to check Bluetooth permission
    fun checkBluetoothPermission(context: Context): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request Bluetooth permission
    fun requestBluetoothPermission(context: Context) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        ActivityCompat.requestPermissions(context as android.app.Activity, permissions, 100)
    }

    // Function to check location permission
    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request location permission
    fun requestLocationPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as android.app.Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            101
        )
    }
    // Function to stop BLE scanning
    fun stopScan() {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback?.let {
            scanner.stopScan(it)
            Log.d("BLE_SCAN", "Scanning stopped")
        }
        isScanning = false
    }
    // Function to start BLE scanning
    fun startScan(context: Context) {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Toast.makeText(context, "BLE Scanner is not available.", Toast.LENGTH_SHORT).show()
            return
        }
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                try {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        val deviceName = result.device.name
                        val deviceAddress = result.device.address

                        if (deviceName.isNullOrBlank() || uniqueDevices.value.contains(deviceAddress)) {
                            Log.d("BLE_SCAN", "Skipping device: $deviceAddress")
                            return
                        }

                        uniqueDevices.value.add(deviceAddress)
                        devices.add(BleDevice(deviceName, deviceAddress, result.rssi))
                        Log.d("BLE_SCAN", "Device added: $deviceName - $deviceAddress")
                    } else {
                        Log.w("BLE_SCAN", "Missing BLUETOOTH_CONNECT permission")
                    }
                } catch (e: SecurityException) {
                    Log.e("BLE_SCAN", "SecurityException: ${e.message}")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE_SCAN", "Scan failed with error code: $errorCode")
                Toast.makeText(context, "BLE Scan failed. Error: $errorCode", Toast.LENGTH_SHORT).show()
            }
        }

        scanner.startScan(callback)
        scanCallback = callback
        isScanning = true
        Log.d("BLE_SCAN", "Scanning started")

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                contentDescription = "Bluetooth Icon",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan BLE",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold
            )
        }

        // Display the number of scanned devices
        if (devices.isNotEmpty()) {
            Text(
                text = "Appareils trouvés: ${devices.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Scanning Button
        Button(
            onClick = {
                if (isScanning) {
                    stopScan()
                    isScanning = false
                } else {
                    devices.clear()
                    uniqueDevices.value.clear()
                    if (!checkBluetoothPermission(context)) {
                        requestBluetoothPermission(context)
                    } else if (!checkLocationPermission(context)) {
                        requestLocationPermission(context)
                    } else {
                        startScan(context)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text(if (isScanning) "Arrêter" else "Scanner")
        }

        // Device List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { device ->
                DeviceItem(
                    deviceName = device.name,
                    deviceAddress = device.address,
                    rssi = device.rssi,
                    onClick = {
                        navController.navigate("device/${device.address}")
                    }
                )
            }
        }
    }
}

// Composable function to display a single BLE device
@Composable
fun DeviceItem(
    deviceName: String, // Name of the device
    deviceAddress: String, // MAC address
    rssi: Int, // Signal strength
    onClick: () -> Unit // Click action
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Rounded corners for the card
            .clickable(onClick = onClick), // Make the card clickable
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // Elevation for the card
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Space between elements
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            Column {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deviceAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Signal strength indicator with color coding
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        rssi > -60 -> Color(0xFF4CAF50) // Green for strong signal
                        rssi > -80 -> Color(0xFFFFC107) // Yellow for medium signal
                        else -> Color(0xFFF44336) // Red for weak signal
                    }
                ),
                shape = RoundedCornerShape(4.dp) // Rounded corners
            ) {
                Text(
                    text = "$rssi dBm", // Display RSSI value
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
