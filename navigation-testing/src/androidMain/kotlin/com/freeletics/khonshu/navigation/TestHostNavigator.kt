package com.freeletics.khonshu.navigation

import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.parcelize.Parcelize

public class TestHostNavigator() : HostNavigator() {
    @InternalNavigationCodegenApi
    private val fakeEntry = StackEntry.create(StackEntry.Id(""), DummyRoute)

    private val eventTurbine = Turbine<TestEvent>()
    internal val events: Flow<TestEvent>
        get() = eventTurbine.asChannel().receiveAsFlow()

    internal val savedStateHandle: SavedStateHandle
        @OptIn(InternalNavigationCodegenApi::class)
        get() = fakeEntry.savedStateHandle

    @InternalNavigationCodegenApi
    @InternalNavigationTestingApi
    override val snapshot: State<StackSnapshot>
        get() = throw UnsupportedOperationException()

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

    @Parcelize
    private object DummyRoute : NavRoute
}
