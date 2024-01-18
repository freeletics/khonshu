package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.util.androidxNavHost
import com.freeletics.khonshu.codegen.util.experimentalNavHost
import com.freeletics.khonshu.codegen.util.getComponent
import com.freeletics.khonshu.codegen.util.getComponentFromRoute
import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.overlayDestination
import com.freeletics.khonshu.codegen.util.screenDestination
import com.squareup.anvil.compiler.internal.capitalize
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

    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : BaseData

public data class NavHostActivityData(
    private val originalName: String,
    override val packageName: String,

    override val scope: ClassName,
    override val parentScope: ClassName,

    override val stateMachine: ClassName,

    public val activityBaseClass: ClassName,

    val experimentalNavigation: Boolean,
    val navHostParameter: ComposableParameter,
    override val stateParameter: ComposableParameter?,
    override val sendActionParameter: ComposableParameter?,
    override val composableParameter: List<ComposableParameter>,
) : BaseData {
    override val navigation: Navigation? = null

    override val baseName: String = when (experimentalNavigation) {
        false -> originalName
        true -> "Experimental${originalName.capitalize()}"
    }

    val navHost: MemberName = when (experimentalNavigation) {
        false -> androidxNavHost
        true -> experimentalNavHost
    }
}

public data class Navigation(
    val route: ClassName,
    private val parentScopeIsRoute: Boolean,
    private val overlay: Boolean,
    val destinationScope: ClassName,
) {
    val destinationClass: ClassName = navigationDestination

    val parentComponentLookup: MemberName = when (parentScopeIsRoute) {
        false -> getComponent
        true -> getComponentFromRoute
    }

    val destinationMethod: MemberName = when (overlay) {
        false -> screenDestination
        true -> overlayDestination
    }
}
