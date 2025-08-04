package com.example.newheadlineapp.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

//###########################################################################################################
//                                            FingerScanScreen                                              #
// Contains composable for authentication Screen.                                                           #
// Deals with authentication process. If fingerprint is available requests scan,                          #
// otherwise skips to newsScreen.                                                                          #
//###########################################################################################################

@Composable
fun AuthScreen(navController: NavController, activity: AppCompatActivity) {

    val promptManager by lazy {
        BiometricPromptManager(activity)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.DarkGray
    ) {
        val biometricResult by promptManager.promptResults.collectAsState(initial = null)

        // Launcher for biometric enrollment activity (system settings)
        val enrollLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {}
        )

        // Side effect that triggers when biometricResult changes
        LaunchedEffect((biometricResult)) {
            // If biometric authentication is not set up, launch the system enrollment screen
            if (biometricResult == BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                enrollLauncher.launch(enrollIntent)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = " Welcome To The TOP News APP ",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
                    .fillMaxWidth(0.7f),
                textAlign = TextAlign.Center
                )
            // Button that triggers biometric authentication prompt on click
            Button(
                onClick = {
                promptManager.showBiometricPrompt(
                    title = "Please Authenticate",
                    description = "Authenticate to continue using APP"
                )},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.DarkGray
                ),
                modifier = Modifier
                    .size(width = 200.dp, height = 48.dp)
            ) { Text(
                text = "Authenticate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,) }

            // Display feedback or navigate based on biometric authentication result
            biometricResult?.let { result ->
                when(result) {
                    is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                        Text("Authentication error: ${result.error}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top=8.dp),
                            color = Color.Red,
                            textAlign = TextAlign.Center)
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                        Text("Authentication Failed",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top=8.dp),
                            color = Color.Red,
                            textAlign = TextAlign.Center)
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                        Text("Authentication not set",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top=8.dp),
                            color = Color.Red,
                            textAlign = TextAlign.Center)
                    }
                    //Jump to news if authentication succeeded
                    BiometricPromptManager.BiometricResult.AuthenticationSucceeded -> {
                        navController.navigate("news"){
                            popUpTo("auth") {inclusive=true}
                            launchSingleTop = true
                        }
                    }
                    //Jump straight news if authentication is not supported
                    BiometricPromptManager.BiometricResult.FeatureNotSupported -> {
                        navController.navigate("news"){
                            popUpTo("auth") {inclusive=true}
                            launchSingleTop = true
                        }
                    }
                    //Jump straight to news if authentication hardware is not available
                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                        navController.navigate("news"){
                            popUpTo("auth") {inclusive=true}
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    // Channel to send authentication results asynchronously
    private val resultChannel = Channel<BiometricResult>()

    // Expose authentication results as a Flow to observe in Compose
    val promptResults = resultChannel.receiveAsFlow()

    //Shows the biometric prompt to the user with given title and description
    //Checks for hardware availability, enrollment, and feature support before showing prompt
    fun showBiometricPrompt(
        title: String,
        description: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false)

        // Check biometric hardware and enrollment state and return failed results immediately
        when(manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureNotSupported)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        // Create BiometricPrompt with callback to listen for authentication events
        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(BiometricResult.AuthenticationSucceeded)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )

        // Start biometric authentication prompt
        prompt.authenticate(promptInfo.build())
    }

    // Sealed interface representing possible biometric authentication outcomes
    sealed interface BiometricResult {
        data object HardwareUnavailable : BiometricResult
        data object FeatureNotSupported : BiometricResult
        data class AuthenticationError(val error: String) : BiometricResult
        data object AuthenticationSucceeded : BiometricResult
        data object AuthenticationFailed : BiometricResult
        data object AuthenticationNotSet : BiometricResult
    }
}