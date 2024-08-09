package com.freeletics.khonshu.navigation.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class DelegatingOnBackPressedCallbackTest {
    private val underTest = DelegatingOnBackPressedCallback()

    @Test
    fun `test callback disabled by default`() {
        assertThat(underTest.isEnabled).isFalse()
    }

    @Test
    fun `test callback is enabled after adding a callback`() {
        underTest.addCallback { }
        assertThat(underTest.isEnabled).isTrue()
    }

    @Test
    fun `test callback is enabled after adding multiple callbacks`() {
        underTest.addCallback { }
        underTest.addCallback { }
        underTest.addCallback { }
        underTest.addCallback { }
        assertThat(underTest.isEnabled).isTrue()
    }

    @Test
    fun `test callback is disabled after removing all callbacks`() {
        val callback1 = {}
        val callback2 = {}

        underTest.addCallback(callback1)
        underTest.addCallback(callback2)
        assertThat(underTest.isEnabled).isTrue()

        underTest.removeCallback(callback1)
        underTest.removeCallback(callback2)
        assertThat(underTest.isEnabled).isFalse()
    }

    @Test
    fun `test callback is invoked when handleOnBackPressed is called`() {
        var invoked = 0
        underTest.addCallback { invoked++ }

        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        assertThat(invoked).isEqualTo(3)
    }

    @Test
    fun `latest test callback is invoked when handleOnBackPressed is called`() {
        var invoked1 = 0
        underTest.addCallback { invoked1++ }
        var invoked2 = 0
        underTest.addCallback { invoked2++ }

        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        assertThat(invoked1).isEqualTo(0)
        assertThat(invoked2).isEqualTo(3)
    }

    @Test
    fun `previous test callback is invoked after removal of newer callback`() {
        var invoked1 = 0
        underTest.addCallback { invoked1++ }
        var invoked2 = 0
        val callback2: () -> Unit = { invoked2++ }
        underTest.addCallback(callback2)

        underTest.handleOnBackPressed()
        underTest.removeCallback(callback2)
        assertThat(invoked1).isEqualTo(0)
        assertThat(invoked2).isEqualTo(1)

        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        assertThat(invoked1).isEqualTo(2)
        assertThat(invoked2).isEqualTo(1)
    }

    @Test
    fun `nothing happens if there is no callback when handleOnBackPressedIsCalled`() {
        var invoked = 0
        val callback: () -> Unit = { invoked++ }
        underTest.addCallback(callback)

        underTest.handleOnBackPressed()
        underTest.removeCallback(callback)
        assertThat(invoked).isEqualTo(1)

        underTest.handleOnBackPressed()
        underTest.handleOnBackPressed()
        assertThat(invoked).isEqualTo(1)
    }
}
