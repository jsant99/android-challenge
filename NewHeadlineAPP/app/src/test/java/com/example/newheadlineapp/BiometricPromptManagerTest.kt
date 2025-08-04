package com.example.newheadlineapp

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.example.newheadlineapp.ui.BiometricPromptManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class BiometricPromptManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var activity: AppCompatActivity
    private lateinit var biometricManager: BiometricManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        activity = mockk(relaxed = true)
        biometricManager = mockk()

        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(activity) } returns biometricManager
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Hardware Unavailable Scenario`() = runTest {
        // Verify that BiometricResult. is emitted when BiometricManager.canAuthenticate returns BIOMETRIC_ERROR_NO_HARDWARE.
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE

        val manager = BiometricPromptManager(activity)

        manager.promptResults.test(timeout = 2.seconds) {
            manager.showBiometricPrompt("Test Title", "Test Description")
            val result = awaitItem()
            assertEquals(BiometricPromptManager.BiometricResult.HardwareUnavailable, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Feature Not Supported Scenario`() = runTest{
        // Verify that BiometricResult.FeatureNotSupported is emitted when BiometricManager.canAuthenticate returns BIOMETRIC_ERROR_NO_HARDWARE.
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        val manager = BiometricPromptManager(activity)

        manager.promptResults.test(timeout = 2.seconds) {
            manager.showBiometricPrompt("Test Title", "Test Description")
            val result = awaitItem()
            assertEquals(BiometricPromptManager.BiometricResult.FeatureNotSupported, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Authentication Not Set Scenario`() = runTest{
        // Verify that BiometricResult.AuthenticationNotSet is emitted when BiometricManager.canAuthenticate returns BIOMETRIC_ERROR_NONE_ENROLLED.
        every {
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

        val manager = BiometricPromptManager(activity)

        manager.promptResults.test(timeout = 2.seconds) {
            manager.showBiometricPrompt("Test Title", "Test Description")
            val result = awaitItem()
            assertEquals(BiometricPromptManager.BiometricResult.AuthenticationNotSet, result)
            cancelAndIgnoreRemainingEvents()
        }
    }
}