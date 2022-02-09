package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.CommonData.Navigation
import com.squareup.kotlinpoet.ClassName

internal sealed interface BaseData {
    val baseName: String
    val packageName: String
}

internal sealed interface CommonData : BaseData {
    val scope: ClassName

    val parentScope: ClassName
    val dependencies: ClassName

    val stateMachine: ClassName

    val navigation: Navigation?

    val coroutinesEnabled: Boolean
    val rxJavaEnabled: Boolean

    data class Navigation(
        val navigator: ClassName,
        val navRoute: ClassName?,
        val navRoot: ClassName?,
    )
}

internal data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,

    override val navigation: Navigation?,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) :  CommonData

internal data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,
    val fragmentBaseClass: ClassName,

    override val navigation: Navigation?,

    val enableInsetHandling: Boolean,
    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) : CommonData

internal data class RendererFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,
    val factory: ClassName,
    val fragmentBaseClass: ClassName,

    override val navigation: Navigation?,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) : CommonData

internal data class NavEntryData(
    override val baseName: String,
    override val packageName: String,

    val scope: ClassName,

    val parentScope: ClassName,

    val coroutinesEnabled: Boolean,
    val rxJavaEnabled: Boolean,
): BaseData
