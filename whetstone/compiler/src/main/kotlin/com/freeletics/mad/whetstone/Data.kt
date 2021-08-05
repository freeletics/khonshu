package com.freeletics.mad.whetstone

import com.squareup.kotlinpoet.ClassName

data class Data(
    val baseName: String,
    val packageName: String,

    val scope: ClassName,

    val parentScope: ClassName,
    val dependencies: ClassName,

    val stateMachine: ClassName,

    val navigation: Navigation?,

    val coroutinesEnabled: Boolean,
    val rxJavaEnabled: Boolean,

    val extra: Extra?
)

data class Navigation(
    val navigator: ClassName,
    val navigationHandler: ClassName,
)

sealed class Extra {
    data class Compose(val withFragment: Boolean) : Extra()
    data class Renderer(val factory: ClassName) : Extra()
}
