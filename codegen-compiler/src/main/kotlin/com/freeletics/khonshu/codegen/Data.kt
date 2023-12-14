package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.util.composeDestination
import com.freeletics.khonshu.codegen.codegen.util.composeOverlayDestination
import com.freeletics.khonshu.codegen.codegen.util.composeScreenDestination
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

    override val navigation: Navigation.Compose,

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

    val navHostParameter: ComposableParameter,
    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : ComposeData {
    override val navigation: Navigation? = null
}

public sealed interface Navigation {
    public val route: ClassName
    public val parentScopeIsRoute: Boolean
    public val destinationClass: ClassName
    public val destinationScope: ClassName
    public val destinationMethod: MemberName?

    public data class Compose(
        override val route: ClassName,
        override val parentScopeIsRoute: Boolean,
        private val overlay: Boolean,
        override val destinationScope: ClassName,
    ) : Navigation {
        override val destinationClass: ClassName = composeDestination

        override val destinationMethod: MemberName = when (overlay) {
            false -> composeScreenDestination
            true -> composeOverlayDestination
        }
    }
}
