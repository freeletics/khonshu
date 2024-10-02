package com.freeletics.khonshu.navigation

import android.content.Intent
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
import app.cash.turbine.Turbine
import app.cash.turbine.plusAssign
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.MultiNavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.NavigateToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.UpEvent
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

public class TestHostNavigator(
    public var handleDeepLinkRoute: NavRoute? = null,
) : HostNavigator() {
    internal val events = Turbine<NavEvent>()
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
        events += MultiNavEvent(TestHostNavigator().apply(block).events.toList())
    }

    override fun navigateTo(route: NavRoute) {
        events += NavigateToEvent(route)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        events += NavEvent.NavigateToRootEvent(root, restoreRootState)
    }

    override fun navigateUp() {
        events += UpEvent
    }

    override fun navigateBack() {
        events += BackEvent
    }

    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        events += BackToEvent(popUpTo, inclusive)
    }

    override fun resetToRoot(root: NavRoot) {
        events += NavEvent.ResetToRoot(root)
    }

    override fun replaceAll(root: NavRoot) {
        events += NavEvent.ReplaceAll(root)
    }

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        events += NavEvent.DestinationResultEvent(key, result)
    }

    @InternalNavigationApi
    @InternalNavigationCodegenApi
    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        return NavigationResultRequest(key, SavedStateHandle())
    }

    override fun backPresses(): Flow<Unit> {
        return backPresses.asChannel().receiveAsFlow()
    }

    override fun <T> backPresses(value: T): Flow<T> {
        return backPresses.asChannel().receiveAsFlow().map { value }
    }

    private fun <T> Turbine<T>.toList(): List<T> {
        close()
        return buildList {
            do {
                val event = takeEvent()
                if (event is Event.Item) {
                    add(event.value)
                }
            } while (event is Event.Item)
        }
    }
}
