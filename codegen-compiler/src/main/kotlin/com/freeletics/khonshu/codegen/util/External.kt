package com.freeletics.khonshu.codegen.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.UNIT

// Codegen Public API
internal val overlay = ClassName("com.freeletics.khonshu.codegen", "Overlay")

// Codegen Internal API
internal val asComposeState = MemberName("com.freeletics.khonshu.codegen.internal", "asComposeState")
internal val InternalCodegenApi = ClassName("com.freeletics.khonshu.codegen.internal", "InternalCodegenApi")
internal val getComponent = MemberName("com.freeletics.khonshu.codegen.internal", "component")
internal val getComponentFromRoute = MemberName("com.freeletics.khonshu.codegen.internal", "componentFromParentRoute")
internal val componentProvider = ClassName("com.freeletics.khonshu.codegen.internal", "ComponentProvider")
internal val activityComponentProvider =
    ClassName("com.freeletics.khonshu.codegen.internal", "ActivityComponentProvider")
internal val localActivityComponentProvider =
    MemberName("com.freeletics.khonshu.codegen.internal", "LocalActivityComponentProvider")

// Navigator
internal val stackSnapshot = ClassName("com.freeletics.khonshu.navigation.internal", "StackSnapshot")
internal val stackEntry = ClassName("com.freeletics.khonshu.navigation.internal", "StackEntry")
internal val baseRoute = ClassName("com.freeletics.khonshu.navigation", "BaseRoute")
internal val navRoot = ClassName("com.freeletics.khonshu.navigation", "NavRoot")
internal val activityNavigator = ClassName("com.freeletics.khonshu.navigation", "ActivityNavigator")
internal val hostNavigator = ClassName("com.freeletics.khonshu.navigation", "HostNavigator")
internal val createHostNavigator = MemberName("com.freeletics.khonshu.navigation.internal", "createHostNavigator")
internal val multiStackHostNavigatorViewModel =
    ClassName("com.freeletics.khonshu.navigation.internal", "StackEntryStoreViewModel")
internal val navHost = MemberName("com.freeletics.khonshu.navigation", "NavHost")
internal val navigationSetup = MemberName("com.freeletics.khonshu.navigation", "NavigationSetup")
internal val navigationDestination = ClassName("com.freeletics.khonshu.navigation", "NavDestination")
internal val screenDestination = MemberName("com.freeletics.khonshu.navigation", "ScreenDestination")
internal val overlayDestination = MemberName("com.freeletics.khonshu.navigation", "OverlayDestination")
internal val internalNavigatorApi =
    ClassName("com.freeletics.khonshu.navigation.internal", "InternalNavigationCodegenApi")

internal val simpleNavHost = ClassName("com.freeletics.khonshu.codegen", "SimpleNavHost")
internal val simpleNavHostParameterized = simpleNavHost.parameterizedBy(
    ClassName("androidx.compose.ui", "Modifier"),
    LambdaTypeName.get(null, navRoot, baseRoute, returnType = UNIT).copy(nullable = true),
    UNIT,
)
internal val simpleNavHostLambda = LambdaTypeName.get(
    null,
    ClassName("androidx.compose.ui", "Modifier"),
    LambdaTypeName.get(null, navRoot, baseRoute, returnType = UNIT).copy(nullable = true),
    returnType = UNIT,
)

// StateMachine
internal val stateMachine = ClassName("com.freeletics.khonshu.statemachine", "StateMachine")

// Kotlin
internal val optIn = ClassName("kotlin", "OptIn")
internal val function1 = ClassName("kotlin", "Function1")
internal val function2 = ClassName("kotlin", "Function2")
internal val function3 = ClassName("kotlin", "Function3")

// Coroutines
internal val launch = MemberName("kotlinx.coroutines", "launch")

// Collections Immutable
internal val immutableSet = ClassName("kotlinx.collections.immutable", "ImmutableSet")
internal val toImmutableSet = MemberName("kotlinx.collections.immutable", "toImmutableSet")

// Dagger
internal val provides = ClassName("dagger", "Provides")
internal val multibinds = ClassName("dagger.multibindings", "Multibinds")
internal val intoSet = ClassName("dagger.multibindings", "IntoSet")
internal val bindsInstance = ClassName("dagger", "BindsInstance")
internal val module = ClassName("dagger", "Module")

// AndroidX
internal val componentActivity = ClassName("androidx.activity", "ComponentActivity")
internal val setContent = MemberName("androidx.activity.compose", "setContent")
internal val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")
internal val savedStateViewModelFactory = ClassName("androidx.lifecycle", "SavedStateViewModelFactory")
internal val viewModelProvider = ClassName("androidx.lifecycle", "ViewModelProvider")

// Compose
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val getValue = MemberName("androidx.compose.runtime", "getValue")
internal val remember = MemberName("androidx.compose.runtime", "remember")
internal val compositionLocalProvider = MemberName("androidx.compose.runtime", "CompositionLocalProvider")
internal val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")

// Android
internal val intent = ClassName("android.content", "Intent")
internal val bundle = ClassName("android.os", "Bundle")
