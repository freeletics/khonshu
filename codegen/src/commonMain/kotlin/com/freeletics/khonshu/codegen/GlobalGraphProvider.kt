package com.freeletics.khonshu.codegen

import kotlin.reflect.KClass

public interface GlobalGraphProvider {
    public fun <T> getGraph(scope: KClass<*>): T
}
