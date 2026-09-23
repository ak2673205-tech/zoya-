package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.IntentAnalyzer
import com.example.ai.IntentType
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
        assertEquals("ANU", appName)
    }

    @Test
    fun `test intent analysis differentiates action vs conversation`() {
        val questionIntent = IntentAnalyzer.analyze("Tell me about YouTube")
        assertEquals(IntentType.GENERAL_CONVERSATION, questionIntent.type)

        val openIntent = IntentAnalyzer.analyze("YouTube kholo")
        assertEquals(IntentType.OPEN_APP, openIntent.type)

        val torchIntent = IntentAnalyzer.analyze("Torch on karo")
        assertEquals(IntentType.TOGGLE_TORCH, torchIntent.type)
        assertEquals("true", torchIntent.target)
    }
}
