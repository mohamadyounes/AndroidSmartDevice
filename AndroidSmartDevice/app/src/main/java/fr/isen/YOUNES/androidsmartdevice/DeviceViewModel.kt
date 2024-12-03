package fr.isen.YOUNES.androidsmartdevice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A ViewModel class to manage the state and interactions with the BLE (Bluetooth Low Energy) device.
 * This class acts as a bridge between the UI and the BLE logic in the `BleManager`.
 */
class DeviceViewModel : ViewModel() {

    private var bleManager: BleManager? = null // Reference to the BLE manager for handling BLE operations

    // StateFlows to represent the UI state
    private val _connectionState = MutableStateFlow(BleManager.ConnectionState.DISCONNECTED) // Tracks connection status
    val connectionState: StateFlow<BleManager.ConnectionState> = _connectionState // Exposed read-only connection state

    private val _ledStates = MutableStateFlow(List(3) { false }) // Tracks LED states (3 LEDs as a list of Booleans)
    val ledStates: StateFlow<List<Boolean>> = _ledStates // Exposed read-only LED states

    private val _primaryButtonClicks = MutableStateFlow(0) // Tracks primary button clicks
    val primaryButtonClicks: StateFlow<Int> = _primaryButtonClicks // Exposed read-only primary button clicks

    private val _thirdButtonClicks = MutableStateFlow(0) // Tracks third button clicks
    val thirdButtonClicks: StateFlow<Int> = _thirdButtonClicks // Exposed read-only third button clicks

    /**
     * Initializes the BLE manager and sets up listeners for its state and events.
     *
     * @param context The application context used to initialize the `BleManager`.
     */
    fun initialize(context: Context) {
        try {
            bleManager = BleManager(context) // Create a new instance of the BLE manager

            // Collect the BLE connection state and update the ViewModel's state
            viewModelScope.launch {
                bleManager?.connectionState?.collect { state ->
                    _connectionState.value = state
                }
            }

            // Collect primary button clicks from the BLE manager
            viewModelScope.launch {
                bleManager?.primaryButtonClicks?.collect { clicks ->
                    _primaryButtonClicks.value = clicks
                }
            }

            // Collect third button clicks from the BLE manager
            viewModelScope.launch {
                bleManager?.thirdButtonClicks?.collect { clicks ->
                    _thirdButtonClicks.value = clicks
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = BleManager.ConnectionState.DISCONNECTED // Ensure the connection state is updated in case of failure
        }
    }

    /**
     * Connects to a BLE device using its address.
     *
     * @param address The MAC address of the BLE device.
     */
    fun connectToDevice(address: String) {
        try {
            bleManager?.connect(address) // Attempt to connect to the BLE device
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = BleManager.ConnectionState.DISCONNECTED // Update state if connection fails
        }
    }

    /**
     * Toggles the state of an LED at the specified index.
     *
     * @param index The index of the LED to toggle (0-based).
     */
    fun toggleLed(index: Int) {
        /*try {
            bleManager?.toggleLed(index) // Call the new method in BleManager
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
        try {
            // Update the LED state locally
            val currentStates = _ledStates.value.toMutableList() // Create a mutable copy of the current LED states
            currentStates[index] = !currentStates[index] // Toggle the LED state at the specified index
            _ledStates.value = currentStates // Update the StateFlow with the new states

            // Send the new LED state to the BLE manager
            bleManager?.toggleLed(index, currentStates[index])
        } catch (e: Exception) {
            e.printStackTrace() // Log and handle any potential errors
        }
    }

    /**
     * Disconnects from the BLE device and cleans up resources.
     */
    fun disconnect() {
        try {
            bleManager?.disconnect() // Disconnect from the BLE device
            bleManager = null // Clear the reference to the BLE manager
            _connectionState.value = BleManager.ConnectionState.DISCONNECTED // Update the connection state
        } catch (e: Exception) {
            e.printStackTrace() // Log and handle any potential errors
        }
    }

    /**
     * Called when the ViewModel is being cleared. Ensures that the BLE connection is properly closed.
     */
    override fun onCleared() {
        super.onCleared()
        disconnect() // Clean up BLE resources
    }
}
