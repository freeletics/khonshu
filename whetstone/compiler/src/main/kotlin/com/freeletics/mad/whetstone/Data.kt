package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.util.composeBottomSheetDestination
import com.freeletics.mad.whetstone.codegen.util.composeDestination
import com.freeletics.mad.whetstone.codegen.util.composeDialogDestination
import com.freeletics.mad.whetstone.codegen.util.composeScreenDestination
import com.freeletics.mad.whetstone.codegen.util.fragmentDestination
import com.freeletics.mad.whetstone.codegen.util.fragmentDialogDestination
import com.freeletics.mad.whetstone.codegen.util.fragmentScreenDestination
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal sealed interface BaseData {
    val baseName: String
    val packageName: String

    val scope: ClassName
    val parentScope: ClassName

    val stateMachine: ClassName?
    val navigation: Navigation?
}

internal sealed interface ComposeData : BaseData {
    override val stateMachine: ClassName
    val navEntryData: NavEntryData?
    val composableParameter: List<ClassName>
}

internal data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,

    override val navigation: Navigation.Compose?,
    override val navEntryData: NavEntryData?,
    override val composableParameter: List<ClassName>,
) :  ComposeData

internal sealed interface FragmentData : BaseData {
    val fragmentBaseClass: ClassName
    override val navigation: Navigation.Fragment?
    val navEntryData: NavEntryData?
}

internal data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,
    override val fragmentBaseClass: ClassName,

    override val navigation: Navigation.Fragment?,
    override val navEntryData: NavEntryData?,
    override val composableParameter: List<ClassName>,
) : ComposeData, FragmentData

internal data class RendererFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,
    val factory: ClassName,
    override val fragmentBaseClass: ClassName,

    override val navigation: Navigation.Fragment?,
    override val navEntryData: NavEntryData?,
) : FragmentData

internal data class NavEntryData(
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val navigation: Navigation
): BaseData {
    override val baseName: String = "${scope.simpleName}NavEntry"

    override val stateMachine: ClassName? = null
}

internal sealed interface Navigation {
    val route: ClassName
    val destinationClass: ClassName
    val destinationScope: ClassName
    val destinationMethod: MemberName?

    data class Compose(
        override val route: ClassName,
        private val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = composeDestination

        override val destinationMethod = when(destinationType) {
            "NONE" -> null
            "SCREEN" -> composeScreenDestination
            "DIALOG" -> composeDialogDestination
            "BOTTOM_SHEET" -> composeBottomSheetDestination
            else -> throw IllegalArgumentException("Unknown destinationType $destinationType")
        }
    }

    data class Fragment(
        override val route: ClassName,
        private val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = fragmentDestination

        override val destinationMethod = when(destinationType) {
            "NONE" -> null
            "SCREEN" -> fragmentScreenDestination
            "DIALOG" -> fragmentDialogDestination
            else -> throw IllegalArgumentException("Unknown destinationType $destinationType")
        }
    }
}
