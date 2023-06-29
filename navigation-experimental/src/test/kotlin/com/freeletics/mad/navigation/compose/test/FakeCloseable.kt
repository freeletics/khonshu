package com.freeletics.mad.navigation.compose.test

import java.io.Closeable

internal class FakeCloseable : Closeable {
    var closed = false

    override fun close() {
        closed = true
    }
}
