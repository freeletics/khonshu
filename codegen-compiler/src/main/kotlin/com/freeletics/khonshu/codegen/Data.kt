package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.util.composeDestination
import com.freeletics.khonshu.codegen.codegen.util.composeOverlayDestination
import com.freeletics.khonshu.codegen.codegen.util.composeScreenDestination
import com.freeletics.khonshu.codegen.codegen.util.fragmentDestination
import com.freeletics.khonshu.codegen.codegen.util.fragmentDialogDestination
import com.freeletics.khonshu.codegen.codegen.util.fragmentScreenDestination
import com.freeletics.khonshu.codegen.compose.DestinationType
import com.freeletics.khonshu.codegen.fragment.DestinationType as FragmentDestinationType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName

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
    public val stateParameter: ComposableParameter?
    public val sendActionParameter: ComposableParameter?
    public val composableParameter: List<ComposableParameter>
}

public data class ComposableParameter(
    public val name: String,
    public val typeName: TypeName,
)

public data class ComposeScreenData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,

    override val navigation: Navigation.Compose?,

    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : ComposeData

public data class NavHostActivityData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,

    public val activityBaseClass: ClassName,

    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : ComposeData {
    override val navigation: Navigation? = null
}

public sealed interface FragmentData : BaseData {
    public val fragmentBaseClass: ClassName
    override val navigation: Navigation.Fragment?
}

public data class ComposeFragmentData(
    override val baseName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,
    override val fragmentBaseClass: ClassName,

    override val navigation: Navigation.Fragment?,

    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
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
) : FragmentData

public sealed interface Navigation {
    public val route: ClassName
    public val parentScopeIsRoute: Boolean
    public val destinationClass: ClassName
    public val destinationScope: ClassName
    public val destinationMethod: MemberName?

    public data class Compose(
        override val route: ClassName,
        override val parentScopeIsRoute: Boolean,
        private val destinationType: DestinationType,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = composeDestination

        override val destinationMethod: MemberName = when (destinationType) {
            DestinationType.SCREEN -> composeScreenDestination
            DestinationType.OVERLAY -> composeOverlayDestination
        }
    }

    public data class Fragment(
        override val route: ClassName,
        override val parentScopeIsRoute: Boolean,
        private val destinationType: FragmentDestinationType,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = fragmentDestination

        override val destinationMethod: MemberName = when (destinationType) {
            FragmentDestinationType.SCREEN -> fragmentScreenDestination
            FragmentDestinationType.DIALOG -> fragmentDialogDestination
        }
    }
}
