package com.freeletics.mad.whetstone.codegen.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import org.jetbrains.kotlin.name.FqName

// Whetstone Public API
internal val rendererFragment = ClassName("com.freeletics.mad.whetstone.fragment", "RendererFragment")
internal val rendererFragmentFqName = FqName(rendererFragment.canonicalName)
internal val composeFragment = ClassName("com.freeletics.mad.whetstone.fragment", "ComposeFragment")
internal val composeFragmentFqName = FqName(composeFragment.canonicalName)
internal val fragmentNavDestination = ClassName("com.freeletics.mad.whetstone.fragment", "NavDestination")
internal val fragmentNavDestinationFqName = FqName(fragmentNavDestination.canonicalName)
internal val fragmentRootNavDestination = ClassName("com.freeletics.mad.whetstone.fragment", "RootNavDestination")
internal val fragmentRootNavDestinationFqName = FqName(fragmentRootNavDestination.canonicalName)
internal val compose = ClassName("com.freeletics.mad.whetstone.compose", "ComposeScreen")
internal val composeFqName = FqName(compose.canonicalName)
internal val composeNavDestination = ClassName("com.freeletics.mad.whetstone.compose", "NavDestination")
internal val composeNavDestinationFqName = FqName(composeNavDestination.canonicalName)
internal val composeRootNavDestination = ClassName("com.freeletics.mad.whetstone.compose", "RootNavDestination")
internal val composeRootNavDestinationFqName = FqName(composeRootNavDestination.canonicalName)
internal val scopeTo = ClassName("com.freeletics.mad.whetstone", "ScopeTo")
internal val navEntryComponent = ClassName("com.freeletics.mad.whetstone", "NavEntryComponent")
internal val navEntryComponentFqName = FqName(navEntryComponent.canonicalName)
internal val navEntryIdScope = ClassName("com.freeletics.mad.whetstone", "NavEntryId")

// Whetstone Internal API
internal val asComposeState = MemberName("com.freeletics.mad.whetstone.internal", "asComposeState")
internal val internalWhetstoneApi = ClassName("com.freeletics.mad.whetstone.internal", "InternalWhetstoneApi")
internal val navEntryViewModelProvider = MemberName("com.freeletics.mad.whetstone.internal", "viewModelProvider")
internal val fragmentViewModelProvider = MemberName("com.freeletics.mad.whetstone.fragment.internal", "viewModelProvider")
internal val rememberViewModelProvider = MemberName("com.freeletics.mad.whetstone.internal", "rememberViewModelProvider")
internal val navEntryComponentGetter = ClassName("com.freeletics.mad.whetstone.internal", "NavEntryComponentGetter")
internal val navEntryComponentGetterKey = ClassName("com.freeletics.mad.whetstone.internal", "NavEntryComponentGetterKey")
internal val composeProviderValueModule = ClassName("com.freeletics.mad.whetstone.internal", "ComposeProviderValueModule")

// Navigator
internal val navEventNavigator = ClassName("com.freeletics.mad.navigator", "NavEventNavigator")
internal val composeNavigationHandler = MemberName("com.freeletics.mad.navigator.compose", "NavigationSetup")
internal val fragmentNavigationHandler = MemberName("com.freeletics.mad.navigator.fragment", "handleNavigation")
internal val requireRoute = MemberName("com.freeletics.mad.navigator.fragment", "requireRoute")

// Renderer
internal val rendererConnect = MemberName("com.gabrielittner.renderer.connect", "connect")

// Kotlin
internal val optIn = ClassName("kotlin", "OptIn")

// Coroutines
internal val coroutineScope = ClassName("kotlinx.coroutines", "CoroutineScope")
internal val coroutineScopeCancel = MemberName("kotlinx.coroutines", "cancel")
internal val mainScope = MemberName("kotlinx.coroutines", "MainScope")
internal val launch = MemberName("kotlinx.coroutines", "launch")

// RxJava
internal val compositeDisposable = ClassName("io.reactivex.disposables", "CompositeDisposable")

// Dagger
internal val inject = ClassName("javax.inject", "Inject")
internal val component = ClassName("dagger", "Component")
internal val componentFactory = component.nestedClass("Factory")
internal val provides = ClassName("dagger", "Provides")
internal val intoSet = ClassName("dagger.multibindings", "IntoSet")
internal val bindsInstance = ClassName("dagger", "BindsInstance")
internal val module = ClassName("dagger", "Module")
internal val moduleFqName = FqName(module.canonicalName)

// AndroidX
internal val fragment = ClassName("androidx.fragment.app", "Fragment")
internal val dialogFragment = ClassName("androidx.fragment.app", "DialogFragment")

internal val viewModel = ClassName("androidx.lifecycle", "ViewModel")
internal val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")

internal val navBackStackEntry = ClassName("androidx.navigation", "NavBackStackEntry")

// Compose
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val getValue = MemberName("androidx.compose.runtime", "getValue")
internal val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")
internal val compositionLocalProvider = ClassName("androidx.compose.runtime", "CompositionLocalProvider")
internal val providedValue = ClassName("androidx.compose.runtime", "ProvidedValue")
internal val composeView = ClassName("androidx.compose.ui.platform", "ComposeView")
internal val viewCompositionStrategy = ClassName("androidx.compose.ui.platform", "ViewCompositionStrategy")
internal val disposeOnLifecycleDestroyed = viewCompositionStrategy.nestedClass("DisposeOnLifecycleDestroyed")

// Accompanist
internal val localWindowInsets = ClassName("com.google.accompanist.insets", "LocalWindowInsets")
internal val viewWindowInsetsObserver = ClassName("com.google.accompanist.insets", "ViewWindowInsetObserver")

// Android
internal val layoutInflater = ClassName("android.view", "LayoutInflater")
internal val viewGroup = ClassName("android.view", "ViewGroup")
internal val view = ClassName("android.view", "View")
internal val bundle = ClassName("android.os", "Bundle")
internal val context = ClassName("android.content", "Context")
internal val layoutParams = viewGroup.nestedClass("LayoutParams")
