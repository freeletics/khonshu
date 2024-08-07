package com.freeletics.khonshu.navigation.test

import android.content.Intent
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import app.cash.turbine.Turbine
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.Navigator
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.NavEventCollector
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow

internal class TestHostNavigator : HostNavigator() {
    val received = Turbine<NavEvent>()

    private val entry = TestStackEntryFactory().create(SimpleRoot(0))
    override val snapshot: MutableState<StackSnapshot> = mutableStateOf(StackSnapshot(listOf(entry), entry))

    override fun navigateTo(route: NavRoute) {
        received.add(NavEvent.NavigateToEvent(route))
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        received.add(NavEvent.NavigateToRootEvent(root, restoreRootState))
    }

    override fun navigateUp() {
        received.add(NavEvent.UpEvent)
    }

    override fun navigateBack() {
        received.add(NavEvent.BackEvent)
    }

    override fun <T : BaseRoute> navigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    ) {
        received.add(NavEvent.BackToEvent(popUpTo, inclusive))
    }

    override fun resetToRoot(root: NavRoot) {
        received.add(NavEvent.ResetToRoot(root))
    }

    override fun replaceAll(root: NavRoot) {
        received.add(NavEvent.ReplaceAll(root))
    }

    override fun navigate(block: Navigator.() -> Unit) {
        val events = NavEventCollector().apply(block).navEvents
        received.add(NavEvent.MultiNavEvent(events))
    }

    override fun handleDeepLink(
        intent: Intent,
        deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
        deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
    ): Boolean {
        throw UnsupportedOperationException()
    }

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        throw UnsupportedOperationException()
    }

    @InternalNavigationApi
    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        throw UnsupportedOperationException()
    }

    override val onBackPressedCallback: OnBackPressedCallback
        get() = throw UnsupportedOperationException()

    override fun backPresses(): Flow<Unit> {
        throw UnsupportedOperationException()
    }

    override fun <T> backPresses(value: T): Flow<T> {
        throw UnsupportedOperationException()
    }
}
