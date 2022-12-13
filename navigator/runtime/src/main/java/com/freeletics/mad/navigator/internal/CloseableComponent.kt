package com.freeletics.mad.navigator.internal

import java.io.Closeable

public interface CloseableComponent {
    public val closeables: Set<Closeable>
}
