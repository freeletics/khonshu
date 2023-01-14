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

public sealed interface BaseData {
    public val baseName: String
    public val packageName: String

    public val scope: ClassName
    public val parentScope: ClassName

    public val stateMachine: ClassName?
    public val navigation: Navigation?
}

public sealed interface ComposeData : BaseData {
    override val stateMachine: ClassName
    public val navEntryData: NavEntryData?
    public val composableParameter: List<ComposableParameter>
}

public data class ComposableParameter(
    public val name: String,
    public val className: ClassName
)

public data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,

    override val navigation: Navigation.Compose?,
    override val navEntryData: NavEntryData?,
    override val composableParameter: List<ComposableParameter>,
) :  ComposeData

public sealed interface FragmentData : BaseData {
    public val fragmentBaseClass: ClassName
    override val navigation: Navigation.Fragment?
    public val navEntryData: NavEntryData?
}

public data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,
    override val fragmentBaseClass: ClassName,

    override val navigation: Navigation.Fragment?,
    override val navEntryData: NavEntryData?,
    override val composableParameter: List<ComposableParameter>,
) : ComposeData, FragmentData

public data class RendererFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,
    public val factory: ClassName,
    override val fragmentBaseClass: ClassName,

    override val navigation: Navigation.Fragment?,
    override val navEntryData: NavEntryData?,
) : FragmentData

public data class NavEntryData(
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val navigation: Navigation
): BaseData {
    override val baseName: String = "${scope.simpleName}NavEntry"

    override val stateMachine: ClassName? = null
}

public sealed interface Navigation {
    public val route: ClassName
    public val destinationClass: ClassName
    public val destinationScope: ClassName
    public val destinationMethod: MemberName?

    public data class Compose(
        override val route: ClassName,
        private val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = composeDestination

        override val destinationMethod: MemberName = when(destinationType) {
            "SCREEN" -> composeScreenDestination
            "DIALOG" -> composeDialogDestination
            "BOTTOM_SHEET" -> composeBottomSheetDestination
            else -> throw IllegalArgumentException("Unknown destinationType $destinationType")
        }
    }

    public data class Fragment(
        override val route: ClassName,
        private val destinationType: String,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = fragmentDestination

        override val destinationMethod: MemberName? = when(destinationType) {
            "SCREEN" -> fragmentScreenDestination
            "DIALOG" -> fragmentDialogDestination
            else -> throw IllegalArgumentException("Unknown destinationType $destinationType")
        }
    }
}
