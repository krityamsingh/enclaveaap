package com.enclaveapp

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.enclaveapp.ui.ChatViewModel
import com.enclaveapp.ui.ChatsListScreen
import com.enclaveapp.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel5)
class ChatsScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureChatsScreen() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val mockViewModel = ChatViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatsListScreen(
                    viewModel = mockViewModel,
                    showBottomSheet = false,
                    onDismissBottomSheet = {},
                    onNavigateToChat = {}
                )
            }
        }
        
        // Wait for coroutines and side effects
        composeTestRule.waitForIdle()

        composeTestRule.onRoot()
            .captureRoboImage("src/test/screenshots/ChatsScreenshotTest_captureChatsScreen.png")
    }
}
