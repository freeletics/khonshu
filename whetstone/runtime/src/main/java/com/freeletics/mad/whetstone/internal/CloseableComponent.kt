package com.freeletics.mad.whetstone.internal

import java.io.Closeable

public interface CloseableComponent {
    public val closeables: Set<Closeable>
}
