package fr.isen.YOUNES.androidsmartdevice.composable

// Importing necessary Compose and Android libraries
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.isen.YOUNES.androidsmartdevice.BleManager
import fr.isen.YOUNES.androidsmartdevice.DeviceViewModel

@Composable
fun DeviceScreen(
    deviceAddress: String,
    navController: NavController,
    viewModel: DeviceViewModel = remember { DeviceViewModel() }
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val ledStates by viewModel.ledStates.collectAsState()
    val primaryClicks by viewModel.primaryButtonClicks.collectAsState()
    val thirdClicks by viewModel.thirdButtonClicks.collectAsState()

    var isSubscribed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        viewModel.connectToDevice(deviceAddress)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (connectionState) {
            BleManager.ConnectionState.CONNECTING -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2196F3)
                )
                Text(
                    "Connexion en cours...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            BleManager.ConnectionState.CONNECTED -> {
                Text(
                    "TPBLE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2196F3),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Affichage des différentes LED",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ledStates.forEachIndexed { index, isOn ->
                        CustomLedButton(
                            isOn = isOn,
                            onClick = { viewModel.toggleLed(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Abonnez vous pour recevoir\nle nombre d'incrémentation",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = isSubscribed,
                        onCheckedChange = { isSubscribed = it },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Nombre : ${primaryClicks + thirdClicks}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            BleManager.ConnectionState.DISCONNECTED -> {
                Text(
                    "Déconnecté",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CustomLedButton(
    isOn: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Text(
                text = if (isOn) "💡" else "⚪️",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Text(
            text = "LED",
            style = MaterialTheme.typography.bodySmall,
            color = if (isOn) Color(0xFF2196F3) else Color.Gray
        )
    }
}
