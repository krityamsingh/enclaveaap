package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Enclave", appName) // Updated to match actual app name
  }

  @Test
  fun `launch MainActivity`() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
       scenario.onActivity { activity ->
           // If we get here, no crash on startup
       }
    }
  }
}

