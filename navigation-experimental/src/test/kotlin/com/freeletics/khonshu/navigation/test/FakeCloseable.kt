package com.freeletics.khonshu.navigation.test

import java.io.Closeable

internal class FakeCloseable : Closeable {
    var closed = false

    override fun close() {
        closed = true
    }
}
