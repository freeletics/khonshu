package com.freeletics.mad.whetstone.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import org.jetbrains.kotlin.name.FqName

// Whetstone Public API
internal val retainedComponent = ClassName("com.freeletics.mad.whetstone", "RetainedComponent")
internal val retainedComponentFqName = FqName(retainedComponent.canonicalName)
internal val rendererFragment = ClassName("com.freeletics.mad.whetstone", "RendererFragment")
internal val rendererFragmentFqName = FqName(rendererFragment.canonicalName)
internal val composeFragment = ClassName("com.freeletics.mad.whetstone", "ComposeFragment")
internal val composeFragmentFqName = FqName(composeFragment.canonicalName)
internal val compose = ClassName("com.freeletics.mad.whetstone", "ComposeScreen")
internal val composeFqName = FqName(compose.canonicalName)
internal val scopeTo = ClassName("com.freeletics.mad.whetstone", "ScopeTo")

// Whetstone Internal API
internal val emptyNavigationHandler = ClassName("com.freeletics.mad.whetstone.internal", "EmptyNavigationHandler")
internal val emptyNavigator = ClassName("com.freeletics.mad.whetstone.internal", "EmptyNavigator")
internal val internalWhetstoneApi = ClassName("com.freeletics.mad.whetstone.internal", "InternalWhetstoneApi")
internal val viewModelProvider = MemberName("com.freeletics.mad.whetstone.internal", "viewModelProvider")
internal val rememberViewModelProvider = MemberName("com.freeletics.mad.whetstone.internal", "rememberViewModelProvider")

// Navigator
internal val navigationHandler = ClassName("com.freeletics.mad.navigator", "NavigationHandler")
internal val navigationHandlerHandle = navigationHandler.member("handle")

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
internal val component = ClassName("dagger", "Component")
internal val componentFactory = component.nestedClass("Factory")
internal val subcomponent = ClassName("dagger", "Subcomponent")
internal val subcomponentFactory = subcomponent.nestedClass("Factory")
internal val bindsInstance = ClassName("dagger", "BindsInstance")

// AndroidX
internal val fragment = ClassName("androidx.fragment.app", "Fragment")

internal val onBackPressedDispatcher = ClassName("androidx.activity", "OnBackPressedDispatcher")

internal val viewModel = ClassName("androidx.lifecycle", "ViewModel")
internal val savedStateHandle = ClassName("androidx.lifecycle", "SavedStateHandle")
internal val lifecycleCoroutineScope = MemberName("androidx.lifecycle", "coroutineScope")

internal val navController = ClassName("androidx.navigation", "NavController")
internal val findNavController = MemberName("androidx.navigation.fragment", "findNavController")

internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val launchedEffect = MemberName("androidx.compose.runtime", "LaunchedEffect")
internal val collectAsState = MemberName("androidx.compose.runtime", "collectAsState")
internal val getValue = MemberName("androidx.compose.runtime", "getValue")
internal val rememberCoroutineScope = MemberName("androidx.compose.runtime", "rememberCoroutineScope")
internal val composeView = ClassName("androidx.compose.ui.platform", "ComposeView")

// Android
internal val layoutInflater = ClassName("android.view", "LayoutInflater")
internal val viewGroup = ClassName("android.view", "ViewGroup")
internal val view = ClassName("android.view", "View")
internal val bundle = ClassName("android.os", "Bundle")
