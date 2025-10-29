package com.freeletics.khonshu.navigation

import android.content.Intent
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Turbine
import app.cash.turbine.plusAssign
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

public class TestHostNavigator(
    public var handleDeepLinkRoute: NavRoute? = null,
) : HostNavigator() {
    private val eventTurbine = Turbine<TestEvent>()
    internal val events: Flow<TestEvent>
        get() = eventTurbine.asChannel().receiveAsFlow()

    internal val backPresses = Turbine<Unit>()

    @InternalNavigationCodegenApi
    @InternalNavigationTestingApi
    override val snapshot: State<StackSnapshot>
        get() = throw UnsupportedOperationException()

    @InternalNavigationTestingApi
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backPresses += Unit
        }
    }

    /**
     * The fake implementation will call [navigateTo] with [handleDeepLinkRoute] if
     * the latter is not `null`. Otherwise it will just return false.
     */
    override fun handleDeepLink(
        intent: Intent,
        deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
        deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
    ): Boolean {
        handleDeepLinkRoute?.let {
            navigateTo(it)
            return true
        }
        return false
    }

    override fun navigate(block: Navigator.() -> Unit) {
        eventTurbine += TestHostNavigator().apply(block).eventTurbine.asChannel().toTestEvent()
    }

    override fun navigateTo(route: NavRoute) {
        eventTurbine += NavigateToEvent(route)
    }

    override fun navigateUp() {
        eventTurbine += UpEvent
    }

    override fun navigateBack() {
        eventTurbine += BackEvent
    }

    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        eventTurbine += BackToEvent(popUpTo, inclusive)
    }

    override fun switchBackStack(root: NavRoot) {
        eventTurbine += SwitchBackStackEvent(root)
    }

    override fun showRoot(root: NavRoot) {
        eventTurbine += ShowRootEvent(root)
    }

    override fun replaceAllBackStacks(root: NavRoot) {
        eventTurbine += ReplaceAllBackStacksEvent(root)
    }

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        eventTurbine += DestinationResultEvent(key, result)
    }

    @InternalNavigationApi
    @InternalNavigationCodegenApi
    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(StackEntry.Id(id.route.simpleName!!), requestKey)
        return NavigationResultRequest(key, SavedStateHandle())
    }

    override fun backPresses(): Flow<Unit> {
        return backPresses.asChannel().receiveAsFlow()
    }

    override fun <T> backPresses(value: T): Flow<T> {
        return backPresses.asChannel().receiveAsFlow().map { value }
    }

    @InternalNavigationApi
    @OptIn(InternalNavigationCodegenApi::class)
    override fun <T : BaseRoute> getTopEntryFor(destinationId: DestinationId<T>): StackEntry<T> {
        throw UnsupportedOperationException()
    }

    @InternalNavigationApi
    @OptIn(InternalNavigationCodegenApi::class)
    override fun <T : BaseRoute> getEntryFor(id: StackEntry.Id): StackEntry<T> {
        throw UnsupportedOperationException()
    }
}
