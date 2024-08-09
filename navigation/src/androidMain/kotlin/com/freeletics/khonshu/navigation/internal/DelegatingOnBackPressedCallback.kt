package com.freeletics.khonshu.navigation.internal

import androidx.activity.OnBackPressedCallback

@InternalNavigationApi
public class DelegatingOnBackPressedCallback : OnBackPressedCallback(false) {
    private val callbacks = mutableListOf<() -> Unit>()

    @InternalNavigationApi
    public fun addCallback(callback: () -> Unit) {
        callbacks.add(callback)
        isEnabled = true
    }

    @InternalNavigationApi
    public fun removeCallback(callback: () -> Unit) {
        callbacks.remove(callback)
        isEnabled = callbacks.isNotEmpty()
    }

    override fun handleOnBackPressed() {
        callbacks.lastOrNull()?.invoke()
    }
}
