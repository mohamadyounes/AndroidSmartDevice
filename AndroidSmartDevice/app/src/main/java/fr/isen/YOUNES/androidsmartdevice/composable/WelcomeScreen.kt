package fr.isen.YOUNES.androidsmartdevice.composable

// Import necessary Compose and UI-related libraries
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.isen.YOUNES.androidsmartdevice.R

@Composable
fun WelcomeScreen(navController: NavController) {
    // Main column that holds all the UI elements in a vertical arrangement
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the entire available screen space
            .padding(16.dp) // Add padding around the content
    ) {
        // Top section: Title and Sub-title
        Column(
            modifier = Modifier
                .fillMaxWidth() // Take up the full width of the screen
                .padding(top = 32.dp), // Add spacing at the top
            horizontalAlignment = Alignment.CenterHorizontally // Center align all child elements horizontally
        ) {
            // Main heading text
            Text(
                text = "Bienvenue dans votre", // "Welcome to your"
                style = MaterialTheme.typography.headlineMedium, // Use a large text style
                color = Color(0xFF2196F3), // Blue color
                textAlign = TextAlign.Center, // Center align the text
                modifier = Modifier
                    .fillMaxWidth() // Ensure text spans full width
                    .padding(bottom = 8.dp) // Add spacing below the text
            )

            // Sub-heading text
            Text(
                text = "application Smart Device", // "Smart Device application"
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF2196F3), // Blue color
                textAlign = TextAlign.Center, // Center align the text
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Center section: Instructions and Bluetooth Icon
        Column(
            modifier = Modifier
                .weight(1f) // Use remaining space available for this section
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally, // Center align horizontally
            verticalArrangement = Arrangement.Center // Center align vertically
        ) {
            // Instructional text
            Text(
                text = "Pour démarrer vos interactions avec les appareils BLE environnants cliquer sur commencer",
                // "To start interacting with nearby BLE devices, click Start"
                style = MaterialTheme.typography.bodyLarge, // Standard body text style
                color = Color.Black, // Black color for readability
                modifier = Modifier.padding(horizontal = 16.dp), // Add horizontal padding for spacing
                textAlign = TextAlign.Center // Center align the text
            )

            Spacer(modifier = Modifier.height(32.dp)) // Add spacing below the text

            // Bluetooth icon
            Image(
                painter = painterResource(id = R.drawable.bluetooth_icon), // Load Bluetooth icon drawable resource
                contentDescription = "Bluetooth Icon", // Accessibility description
                modifier = Modifier.size(120.dp) // Set the size of the icon
            )
        }

        // Bottom section: Button to navigate to the ScanScreen
        Box(
            modifier = Modifier
                .fillMaxWidth() // Make the box span the full width
                .padding(top = 16.dp) // Add spacing above the button
        ) {
            // Button to navigate to the BLE scan screen
            Button(
                onClick = { navController.navigate("scan") }, // Navigate to the "scan" route on click
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3) // Set the button background color to blue
                ),
                modifier = Modifier.fillMaxWidth() // Make the button span the full width
            ) {
                // Button text
                Text(
                    "COMMENCER", // "START"
                    style = MaterialTheme.typography.labelLarge, // Use label text style
                    color = Color.White // White text for contrast
                )
            }
        }
    }
}
