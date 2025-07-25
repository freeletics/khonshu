package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.overlayDestination
import com.freeletics.khonshu.codegen.util.screenDestination
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName

public sealed interface BaseData {
    public val baseName: String
    public val packageName: String

    public val scope: ClassName
    public val parentScope: ClassName

    public val stateMachine: ClassName

    public val navigation: Navigation?

    public val stateMachineClass: ClassName
    public val stateParameter: ComposableParameter?
    public val sendActionParameter: ComposableParameter?
    public val composableParameter: List<ComposableParameter>
}

public data class ComposableParameter(
    public val name: String,
    public val typeName: TypeName,
)

public data class NavDestinationData(
    override val baseName: String,
    override val packageName: String,
    override val scope: ClassName,
    override val parentScope: ClassName,
    override val stateMachine: ClassName,
    override val navigation: Navigation,
    override val stateMachineClass: ClassName,
    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : BaseData

public data class NavHostActivityData(
    override val baseName: String,
    override val packageName: String,
    override val scope: ClassName,
    override val parentScope: ClassName,
    override val stateMachine: ClassName,
    public val activityBaseClass: ClassName,
    val navHostParameter: ComposableParameter,
    override val stateMachineClass: ClassName,
    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : BaseData {
    override val navigation: Navigation? = null
}

public data class Navigation(
    val route: ClassName,
    val parentScopeIsRoute: Boolean,
    private val overlay: Boolean,
    val destinationScope: ClassName,
) {
    val destinationClass: ClassName = navigationDestination

    val destinationMethod: MemberName = when (overlay) {
        false -> screenDestination
        true -> overlayDestination
    }
}
