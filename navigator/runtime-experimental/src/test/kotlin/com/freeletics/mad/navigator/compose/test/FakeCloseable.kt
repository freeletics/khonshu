package com.freeletics.mad.navigator.compose.test

import java.io.Closeable

internal class FakeCloseable : Closeable {
    var closed = false

    override fun close() {
        closed = true
    }
}
