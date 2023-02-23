package com.freeletics.mad.whetstone.codegen.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import org.jetbrains.kotlin.name.FqName

// Whetstone Public API
internal val rendererFragment = ClassName("com.freeletics.mad.whetstone.fragment", "RendererFragment")
internal val rendererFragmentFqName = FqName(rendererFragment.canonicalName)
internal val composeFragment = ClassName("com.freeletics.mad.whetstone.fragment", "ComposeFragment")
internal val composeFragmentFqName = FqName(composeFragment.canonicalName)
internal val rendererFragmentDestination = ClassName("com.freeletics.mad.whetstone.fragment", "RendererDestination")
internal val rendererFragmentDestinationFqName = FqName(rendererFragmentDestination.canonicalName)
internal val composeFragmentDestination = ClassName("com.freeletics.mad.whetstone.fragment", "ComposeDestination")
internal val composeFragmentDestinationFqName = FqName(composeFragmentDestination.canonicalName)
internal val compose = ClassName("com.freeletics.mad.whetstone.compose", "ComposeScreen")
internal val composeFqName = FqName(compose.canonicalName)
internal val whetstoneComposeDestination = ClassName("com.freeletics.mad.whetstone.compose", "ComposeDestination")
internal val whetstoneComposeScreenDestinationFqName = FqName(whetstoneComposeDestination.canonicalName)
internal val scopeTo = ClassName("com.freeletics.mad.whetstone", "ScopeTo")
internal val appScope = ClassName("com.freeletics.mad.whetstone", "AppScope")
internal val navEntry = ClassName("com.freeletics.mad.whetstone", "NavEntry")
internal val navEntryComponent = ClassName("com.freeletics.mad.whetstone", "NavEntryComponent")
internal val navEntryComponentFqName = FqName(navEntryComponent.canonicalName)

// Whetstone Internal API
internal val asComposeState = MemberName("com.freeletics.mad.whetstone.internal", "asComposeState")
internal val internalWhetstoneApi = ClassName("com.freeletics.mad.whetstone.internal", "InternalWhetstoneApi")
internal val getNavEntryComponent = MemberName("com.freeletics.mad.whetstone.internal", "navEntryComponent")
internal val fragmentComponent = MemberName("com.freeletics.mad.whetstone.fragment.internal", "component")
internal val rememberComponent = MemberName("com.freeletics.mad.whetstone.compose.internal", "rememberComponent")
internal val navEntryComponentGetter = ClassName("com.freeletics.mad.whetstone.internal", "NavEntryComponentGetter")
internal val navEntryComponentGetterKey = ClassName("com.freeletics.mad.whetstone.internal", "NavEntryComponentGetterKey")
internal val destinationComponent = ClassName("com.freeletics.mad.whetstone.internal", "DestinationComponent")

// Navigator
internal val navEventNavigator = ClassName("com.freeletics.mad.navigator", "NavEventNavigator")
internal val navigationExecutor = ClassName("com.freeletics.mad.navigator.internal", "NavigationExecutor")
internal val composeNavigationHandler = MemberName("com.freeletics.mad.navigator.compose", "NavigationSetup")
internal val composeDestination = ClassName("com.freeletics.mad.navigator.compose", "NavDestination")
internal val composeScreenDestination = MemberName("com.freeletics.mad.navigator.compose", "ScreenDestination")
internal val composeDialogDestination = MemberName("com.freeletics.mad.navigator.compose", "DialogDestination")
internal val composeBottomSheetDestination = MemberName("com.freeletics.mad.navigator.compose", "BottomSheetDestination")
internal val fragmentNavigationHandler = MemberName("com.freeletics.mad.navigator.fragment", "handleNavigation")
internal val fragmentDestination = ClassName("com.freeletics.mad.navigator.fragment", "NavDestination")
internal val fragmentScreenDestination = MemberName("com.freeletics.mad.navigator.fragment", "ScreenDestination")
internal val fragmentDialogDestination = MemberName("com.freeletics.mad.navigator.fragment", "DialogDestination")
internal val fragmentRequireRoute = MemberName("com.freeletics.mad.navigator.fragment", "requireRoute")
internal val internalNavigatorApi = ClassName("com.freeletics.mad.navigator.internal", "InternalNavigatorApi")

// StateMachine
internal val stateMachine = ClassName("com.freeletics.mad.statemachine", "StateMachine")
internal val stateMachineFqName = FqName(stateMachine.canonicalName)

// Renderer
internal val viewRenderer = ClassName("com.gabrielittner.renderer", "ViewRenderer")
internal val viewRendererFactory = viewRenderer.nestedClass("Factory")
internal val viewRendererFactoryFqName = FqName(viewRendererFactory.canonicalName)
internal val rendererConnect = MemberName("com.gabrielittner.renderer.connect", "connect")

// Kotlin
internal val optIn = ClassName("kotlin", "OptIn")

// Coroutines
internal val launch = MemberName("kotlinx.coroutines", "launch")

// Dagger
internal val inject = ClassName("javax.inject", "Inject")
internal val provides = ClassName("dagger", "Provides")
internal val multibinds = ClassName("dagger.multibindings", "Multibinds")
internal val intoSet = ClassName("dagger.multibindings", "IntoSet")
internal val bindsInstance = ClassName("dagger", "BindsInstance")
internal val module = ClassName("dagger", "Module")

// AndroidX
internal val fragment = ClassName("androidx.fragment.app", "Fragment")
internal val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")

// Compose
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val getValue = MemberName("androidx.compose.runtime", "getValue")
internal val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")
internal val composeView = ClassName("androidx.compose.ui.platform", "ComposeView")
internal val viewCompositionStrategy = ClassName("androidx.compose.ui.platform", "ViewCompositionStrategy")
internal val disposeOnLifecycleDestroyed = viewCompositionStrategy.nestedClass("DisposeOnViewTreeLifecycleDestroyed")

// Android
internal val layoutInflater = ClassName("android.view", "LayoutInflater")
internal val viewGroup = ClassName("android.view", "ViewGroup")
internal val view = ClassName("android.view", "View")
internal val bundle = ClassName("android.os", "Bundle")
internal val context = ClassName("android.content", "Context")
