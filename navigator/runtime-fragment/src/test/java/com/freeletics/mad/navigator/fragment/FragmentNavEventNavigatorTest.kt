package com.freeletics.mad.navigator.fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.mad.navigator.NavEvent
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

public class FragmentNavEventNavigatorTest {

    private class TestNavigator : FragmentNavEventNavigator()

    @Test
    public fun `navigateBackWithResult is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.resultEvents.test {
            val result = Bundle()
            navigator.navigateBackWithResult("test-key", result)

            assertThat(awaitItem()).isEqualTo(FragmentResultEvent("test-key", result))

            cancel()
        }
    }

    @Test
    public fun `registerForActivityResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.fragmentResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.registerForFragmentResult<String>("test")
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for " +
                "result! You must call this before this navigator gets attached to a " +
                "fragment, e.g. during initialisation of your navigator subclass."
        )
    }
}
