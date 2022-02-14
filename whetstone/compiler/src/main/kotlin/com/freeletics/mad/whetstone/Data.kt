package com.freeletics.mad.whetstone

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
}

internal sealed interface Navigation {
    val route: ClassName
    val destinationType: String
    val destinationScope: ClassName

    data class Compose(
        override val route: ClassName,
        override val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation

    data class Fragment(
        override val route: ClassName,
        override  val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation
}

internal data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,

    override val navigation: Navigation.Compose?,

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

    override val navigation: Navigation.Fragment?,

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

    override val navigation: Navigation.Fragment?,

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
