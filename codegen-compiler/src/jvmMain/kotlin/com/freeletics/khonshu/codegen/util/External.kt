package com.freeletics.khonshu.codegen.util

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.WildcardTypeName

// Codegen Public API
internal val overlay = ClassName("com.freeletics.khonshu.codegen", "Overlay")
internal val globalGraphProvider = ClassName("com.freeletics.khonshu.codegen", "GlobalGraphProvider")

// Codegen Internal API
internal val asComposeState = MemberName("com.freeletics.khonshu.codegen.internal", "asComposeState")
internal val InternalCodegenApi = ClassName("com.freeletics.khonshu.codegen.internal", "InternalCodegenApi")
internal val getGraph = MemberName("com.freeletics.khonshu.codegen.internal", "getGraph")
internal val getGraphFromRoute = MemberName("com.freeletics.khonshu.codegen.internal", "getGraphFromParentRoute")
internal val destinationGraphProvider = ClassName("com.freeletics.khonshu.codegen.internal", "DestinationGraphProvider")
internal val hostGraphProvider =
    ClassName("com.freeletics.khonshu.codegen.internal", "HostGraphProvider")
internal val localHostGraphProvider =
    MemberName("com.freeletics.khonshu.codegen.internal", "LocalHostGraphProvider")

// Navigator
internal val stackSnapshot = ClassName("com.freeletics.khonshu.navigation.internal", "StackSnapshot")
internal val stackEntry = ClassName("com.freeletics.khonshu.navigation.internal", "StackEntry")
internal val baseRoute = ClassName("com.freeletics.khonshu.navigation", "BaseRoute")
internal val navRoot = ClassName("com.freeletics.khonshu.navigation", "NavRoot")
internal val destinationNavigator = ClassName("com.freeletics.khonshu.navigation", "DestinationNavigator")
internal val hostNavigator = ClassName("com.freeletics.khonshu.navigation", "HostNavigator")
internal val createHostNavigator = MemberName("com.freeletics.khonshu.navigation", "createHostNavigator")
internal val stackEntryStoreHolder =
    ClassName("com.freeletics.khonshu.navigation.internal", "StackEntryStoreHolder")
internal val navHost = MemberName("com.freeletics.khonshu.navigation", "NavHost")
internal val activityNavigatorEffect =
    MemberName("com.freeletics.khonshu.navigation.activity", "ActivityNavigatorEffect")
internal val navigationDestination = ClassName(
    "com.freeletics.khonshu.navigation",
    "NavDestination",
).parameterizedBy(WildcardTypeName.producerOf(ANY.copy(nullable = true)))
internal val screenDestination = MemberName("com.freeletics.khonshu.navigation", "ScreenDestination")
internal val overlayDestination = MemberName("com.freeletics.khonshu.navigation", "OverlayDestination")
internal val launchInfo = ClassName("com.freeletics.khonshu.navigation.deeplinks", "LaunchInfo")
internal val asLaunchInfo = MemberName("com.freeletics.khonshu.navigation.deeplinks", "asLaunchInfo")
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
internal val khonshuStateMachine = ClassName("com.freeletics.khonshu.statemachine", "StateMachine")
internal val stateMachineFactory = ClassName("com.freeletics.flowredux2", "FlowReduxStateMachineFactory")
internal val produceStateMachine = MemberName("com.freeletics.flowredux2", "produceStateMachine")

// Kotlin
internal val autoCloseable = ClassName("kotlin", "AutoCloseable")
internal val optIn = ClassName("kotlin", "OptIn")
internal val function1 = ClassName("kotlin", "Function1")
internal val function2 = ClassName("kotlin", "Function2")
internal val function3 = ClassName("kotlin", "Function3")

// Coroutines
internal val launch = MemberName("kotlinx.coroutines", "launch")

// AndroidX
internal val componentActivity = ClassName("androidx.activity", "ComponentActivity")
internal val viewModelStoreOwner = ClassName("androidx.lifecycle", "ViewModelStoreOwner")
internal val setContent = MemberName("androidx.activity.compose", "setContent")
internal val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")

// Compose
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val getValue = MemberName("androidx.compose.runtime", "getValue")
internal val remember = MemberName("androidx.compose.runtime", "remember")
internal val retain = MemberName("androidx.compose.runtime.retain", "retain")
internal val compositionLocalProvider = MemberName("androidx.compose.runtime", "CompositionLocalProvider")
internal val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")

// Android
internal val intent = ClassName("android.content", "Intent")
internal val bundle = ClassName("android.os", "Bundle")
