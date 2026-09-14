package com.freeletics.sample.main

import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
import com.freeletics.khonshu.navigation.deeplinks.handleDeepLink
import com.freeletics.khonshu.statemachine.StateMachine
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface MainState

data object Init : MainState

sealed interface MainAction

@Inject
class MainStateMachine(
    hostNavigator: HostNavigator,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
    launchInfo: LaunchInfo,
) : StateMachine<MainState, MainAction> {
    override val state: StateFlow<MainState>
        field = MutableStateFlow<MainState>(Init)

    init {
        hostNavigator.handleDeepLink(
            launchInfo = launchInfo,
            deepLinkHandlers = deepLinkHandlers,
            deepLinkPrefixes = deepLinkPrefixes,
        )
    }

    override suspend fun dispatch(action: MainAction) {}
}
