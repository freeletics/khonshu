package com.freeletics.khonshu.navigation

import androidx.compose.runtime.State
import app.cash.turbine.Turbine
import app.cash.turbine.plusAssign
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable

public class TestHostNavigator(
    override var startRoot: NavRoot = DummyRoot,
) : HostNavigator() {
    @InternalNavigationCodegenApi
    private val fakeEntry = StackEntry.create(StackEntry.Id(""), DummyRoute)

    private val eventTurbine = Turbine<TestEvent>()
    internal val events: Flow<TestEvent>
        get() = eventTurbine.asChannel().receiveAsFlow()

    @OptIn(InternalNavigationApi::class, InternalNavigationCodegenApi::class)
    internal val state: StackEntryState
        get() = fakeEntry.state

    @InternalNavigationCodegenApi
    @InternalNavigationTestingApi
    override val snapshot: State<StackSnapshot>
        get() = throw UnsupportedOperationException()

    /**
     * Since this navigator does not have a back stack, the returned [DestinationNavigator2] always
     * treats its destination as the current one.
     */
    @InternalNavigationCodegenApi
    override fun destinationNavigator(entry: StackEntry<*>): DestinationNavigator2 {
        return TestDestinationNavigator(isCurrent = true)
    }

    private inner class TestDestinationNavigator(
        private val isCurrent: Boolean,
    ) : DestinationNavigator2,
        Navigator by this@TestHostNavigator {
        override fun navigateUp() {
            if (isCurrent) {
                this@TestHostNavigator.navigateUp()
            }
        }

        override fun navigateBack() {
            if (isCurrent) {
                this@TestHostNavigator.navigateBack()
            }
        }

        override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
            if (isCurrent) {
                this@TestHostNavigator.navigateBackTo(popUpTo, inclusive)
            }
        }

        override fun navigate(block: Navigator.() -> Unit) {
            if (isCurrent) {
                this@TestHostNavigator.navigate(block)
            }
        }
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
        startRoot = root
        eventTurbine += ReplaceAllBackStacksEvent(root)
    }

    @InternalNavigationApi
    @InternalNavigationCodegenApi
    override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
        return fakeEntry
    }

    @InternalNavigationApi
    @InternalNavigationCodegenApi
    override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
        return fakeEntry
    }

    @Serializable
    private object DummyRoot : NavRoot

    @Serializable
    private object DummyRoute : NavRoute
}
