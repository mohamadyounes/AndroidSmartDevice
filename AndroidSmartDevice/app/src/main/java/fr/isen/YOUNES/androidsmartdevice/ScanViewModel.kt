package fr.isen.YOUNES.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<ScanResult>>(emptyList())
    val devices: StateFlow<List<ScanResult>> = _devices

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            viewModelScope.launch {
                val currentList = _devices.value.toMutableList()
                val existingDevice = currentList.find { it.device.address == result.device.address }
                if (existingDevice == null) {
                    currentList.add(result)
                    _devices.value = currentList
                }
            }
        }
    }

    @Suppress("MissingPermission")
    fun startScan(context: Context, bluetoothAdapter: BluetoothAdapter?) {
        if (checkPermissions(context)) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
            } catch (e: SecurityException) {
                // Handle permission denial
            }
        }
    }

    @Suppress("MissingPermission")
    fun stopScan(context: Context, bluetoothAdapter: BluetoothAdapter?) {
        if (checkPermissions(context)) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: SecurityException) {
                // Handle permission denial
            }
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { // For Android 10 and above
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            else -> { // For devices below Android 10
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}
