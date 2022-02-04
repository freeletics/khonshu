package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.util.fragmentNavEventNavigator
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
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

    val navigationEnabled: Boolean
    val navigator: ClassName?

    val coroutinesEnabled: Boolean
    val rxJavaEnabled: Boolean
}

internal data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    override val stateMachine: ClassName,

    override val navigationEnabled: Boolean,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) :  CommonData {
    override val navigator: ClassName?
        get() = navEventNavigator.takeIf { navigationEnabled }
}

internal data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    val fragmentBaseClass: ClassName,

    override val stateMachine: ClassName,

    override val navigationEnabled: Boolean,

    val enableInsetHandling: Boolean,
    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) :  CommonData {
    override val navigator: ClassName?
        get() = fragmentNavEventNavigator.takeIf { navigationEnabled }
}

internal data class RendererFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,

    override val parentScope: ClassName,
    override val dependencies: ClassName,

    val factory: ClassName,
    override val stateMachine: ClassName,
    val fragmentBaseClass: ClassName,

    override val navigationEnabled: Boolean,

    override val coroutinesEnabled: Boolean,
    override val rxJavaEnabled: Boolean,
) : CommonData {
    override val navigator: ClassName?
        get() = fragmentNavEventNavigator.takeIf { navigationEnabled }
}

internal data class NavEntryData(
    override val baseName: String,
    override val packageName: String,

    val scope: ClassName,

    val parentScope: ClassName,

    val coroutinesEnabled: Boolean,
    val rxJavaEnabled: Boolean,
): BaseData
