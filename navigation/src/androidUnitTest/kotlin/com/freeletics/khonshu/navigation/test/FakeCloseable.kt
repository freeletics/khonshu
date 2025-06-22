package com.freeletics.khonshu.navigation.test

internal class FakeCloseable : AutoCloseable {
    var closed = false

    override fun close() {
        closed = true
    }
}
