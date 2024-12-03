package fr.isen.YOUNES.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.YOUNES.androidsmartdevice.composable.DeviceScreen
import fr.isen.YOUNES.androidsmartdevice.composable.ScanScreen
import fr.isen.YOUNES.androidsmartdevice.composable.WelcomeScreen
import fr.isen.YOUNES.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

/**
 * The main activity of the application. This activity serves as the entry point for the app and hosts
 * the navigation logic along with the Compose UI setup.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is created. Sets up the Compose content and applies the app's theme.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sets the content of the activity to use Jetpack Compose
        setContent {
            // Apply the custom theme for the application
            AndroidSmartDeviceTheme {
                // Define a surface that uses the app's background color
                Surface(
                    modifier = Modifier.fillMaxSize(), // Make the surface fill the entire screen
                    color = MaterialTheme.colorScheme.background // Use the theme's background color
                ) {
                    MainApp() // Call the main composable function for the app
                }
            }
        }
    }
}

/**
 * Main composable function that defines the app's navigation flow.
 */
@Composable
fun MainApp() {
    val navController = rememberNavController() // Create a NavController to manage navigation

    // Define the navigation host, starting with the "welcome" screen
    NavHost(
        navController = navController, // Pass the NavController to manage navigation
        startDestination = "welcome" // Set the starting destination of the app
    ) {
        // Define a composable for the "welcome" screen
        composable("welcome") {
            WelcomeScreen(navController = navController) // Pass the NavController to the WelcomeScreen
        }

        // Define a composable for the "scan" screen
        composable("scan") {
            ScanScreen(navController = navController) // Pass the NavController to the ScanScreen
        }

        // Define a composable for the "device" screen, expecting a device address as a parameter
        composable("device/{deviceAddress}") { backStackEntry ->
            // Retrieve the device address from the navigation arguments
            val deviceAddress = backStackEntry.arguments?.getString("deviceAddress")
            deviceAddress?.let { // If the address is not null, navigate to the DeviceScreen
                DeviceScreen(
                    deviceAddress = it, // Pass the retrieved device address
                    navController = navController // Pass the NavController to the DeviceScreen
                )
            }
        }
    }
}
