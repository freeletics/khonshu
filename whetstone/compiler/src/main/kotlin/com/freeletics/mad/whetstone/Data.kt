package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.CommonData.Navigation
import com.squareup.kotlinpoet.ClassName

sealed interface BaseData {
    val baseName: String
    val packageName: String
}

sealed interface CommonData : BaseData {
    val scope: ClassName

    val parentScope: ClassName
    val dependencies: ClassName

    val stateMachine: ClassName

    val navigation: Navigation?

    val coroutinesEnabled: Boolean
    val rxJavaEnabled: Boolean

    data class Navigation(
        val navigator: ClassName,
        val navigationHandler: ClassName,
    )
}

data class ComposeScreenData(
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

data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,
    val enableInsetHandling: Boolean,

    override val navigation: Navigation?,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) :  CommonData

data class RendererFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    val fragmentBaseClass: ClassName,
    val factory: ClassName,
    override val stateMachine: ClassName,

    override val navigation: Navigation?,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) : CommonData

data class NavEntryData(
    override val baseName: String,
    override val packageName: String,

    val scope: ClassName,

    val parentScope: ClassName,

    val coroutinesEnabled: Boolean,
    val rxJavaEnabled: Boolean,
): BaseData
