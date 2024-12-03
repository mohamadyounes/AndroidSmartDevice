package fr.isen.YOUNES.androidsmartdevice

// Import required Android and Bluetooth libraries
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * A manager class for handling BLE (Bluetooth Low Energy) operations such as connecting to devices,
 * discovering services, reading/writing characteristics, and receiving notifications.
 */
class BleManager(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null // Reference to the GATT connection

    // StateFlows to observe connection status and button click counts
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _primaryButtonClicks = MutableStateFlow(0) // Tracks button 1 click count
    val primaryButtonClicks: StateFlow<Int> = _primaryButtonClicks

    private val _thirdButtonClicks = MutableStateFlow(0) // Tracks button 2 click count
    val thirdButtonClicks: StateFlow<Int> = _thirdButtonClicks

    /**
     * UUIDs for services and characteristics, defined as constants for readability and reuse.
     */
    companion object {
        // Service UUIDs
        private val SERVICE_3_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")
        private val SERVICE_4_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb")

        // Characteristic UUIDs
        private val LED_CHARACTERISTIC_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")      // LED control
        private val BUTTON_1_CHARACTERISTIC_UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb") // Button 1
        private val BUTTON_2_CHARACTERISTIC_UUID = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb") // Button 2

        private const val TAG = "BleManager" // Tag for logging
    }

    // Callback to handle Bluetooth GATT events
    private val gattCallback = object : BluetoothGattCallback() {
        // Handle changes in connection state
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> { // Connected to device
                    if (checkBluetoothPermissions()) {
                        _connectionState.value = ConnectionState.CONNECTED
                        try {
                            Log.d(TAG, "Connected to GATT server, discovering services...")
                            gatt.discoverServices() // Start discovering available services
                        } catch (e: SecurityException) {
                            _connectionState.value = ConnectionState.DISCONNECTED
                            Log.e(TAG, "Permission denied: ${e.message}")
                        }
                    } else {
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> { // Disconnected from device
                    _connectionState.value = ConnectionState.DISCONNECTED
                    Log.d(TAG, "Disconnected from GATT server")
                }
            }
        }

        // Handle service discovery
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && checkBluetoothPermissions()) {
                try {
                    // Log discovered services and characteristics
                    gatt.services.forEach { service ->
                        Log.d(TAG, "Discovered Service UUID: ${service.uuid}")
                        service.characteristics.forEach { characteristic ->
                            Log.d(TAG, "  Characteristic UUID: ${characteristic.uuid}")
                        }
                    }

                    enableButtonNotifications(gatt) // Enable notifications for button characteristics
                } catch (e: SecurityException) {
                    Log.e(TAG, "Permission denied: ${e.message}")
                }
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
            }
        }

        // Handle characteristic value changes (notifications)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (!checkBluetoothPermissions()) return

            try {
                when (characteristic.uuid) {
                    BUTTON_1_CHARACTERISTIC_UUID -> { // Button 1 notifications
                        val clicks = characteristic.value[0].toInt()
                        _primaryButtonClicks.value = clicks
                        Log.d(TAG, "Primary button clicks: $clicks")
                    }
                    BUTTON_2_CHARACTERISTIC_UUID -> { // Button 2 notifications
                        val clicks = characteristic.value[0].toInt()
                        _thirdButtonClicks.value = clicks
                        Log.d(TAG, "Third button clicks: $clicks")
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied: ${e.message}")
            }
        }

        // Handle characteristic write operations
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Write successful for characteristic: ${characteristic.uuid}")
            } else {
                Log.e(TAG, "Write failed for characteristic: ${characteristic.uuid}, status: $status")
            }
        }
    }

    // Enable notifications for button characteristics
    private fun enableButtonNotifications(gatt: BluetoothGatt) {
        if (!checkBluetoothPermissions()) return

        try {
            // Enable notifications for Button 1
            gatt.getService(SERVICE_3_UUID)?.getCharacteristic(BUTTON_1_CHARACTERISTIC_UUID)?.let { char ->
                gatt.setCharacteristicNotification(char, true)
            }
            // Enable notifications for Button 2
            gatt.getService(SERVICE_4_UUID)?.getCharacteristic(BUTTON_2_CHARACTERISTIC_UUID)?.let { char ->
                gatt.setCharacteristicNotification(char, true)
            }
        } catch (e: SecurityException) {
            Log.e("BleManager", "Permission denied: ${e.message}")
        }
    }

    // Connect to a BLE device
    fun connect(address: String) {
        if (!checkBluetoothPermissions()) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        try {
            val device = BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(address)
            device?.let {
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
                _connectionState.value = ConnectionState.CONNECTING
            }
        } catch (e: SecurityException) {
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.e("BleManager", "Permission denied: ${e.message}")
        }
    }

    // Disconnect from the BLE device
    fun disconnect() {
        if (!checkBluetoothPermissions()) return

        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt = null
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (e: SecurityException) {
            Log.e("BleManager", "Permission denied: ${e.message}")
        }
    }

    // Toggle the LED state on the device
    @SuppressLint("MissingPermission")
    fun toggleLed(index: Int, isOn: Boolean) {
        val ledCommand = when (index) {
            0 -> if (isOn) 0x01 else 0x00 // LED 1: 0x01 to turn on, 0x00 to turn off
            1 -> if (isOn) 0x02 else 0x00 // LED 2: 0x02
            2 -> if (isOn) 0x03 else 0x00 // LED 3: 0x03
            else -> 0x00 // Default case
        }

        if (!checkBluetoothPermissions()) return

        val service = bluetoothGatt?.services?.getOrNull(2) // Service at index 2
        if (service == null) {
            Log.e("BleManager", "Service at index 2 not found")
            //callback(false)
            return
        }

        // Check if the characteristic at index 0 of the service exists
        val characteristic = service.characteristics.getOrNull(0)
        if (characteristic == null) {
            Log.e("BleManager", "Characteristic at index 0 not found in the service")
            //callback(false)
            return
        }

        // Prepare the command
        characteristic.value = byteArrayOf(ledCommand.toByte())
        val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
        if (success) {
            Log.d("BleManager", "LED command sent successfully: $index")
        } else {
            Log.e("BleManager", "Failed to send LED command")
        }

    }

    // Check if the app has the required Bluetooth permissions
    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Enum representing the connection state
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }
}
