package com.freeletics.sample.screen

import com.freeletics.khonshu.statemachine.StateMachine
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScreenState(val number: Int, val result: String? = null, val locationPermissionGranted: Boolean? = null)

sealed interface ScreenAction {
    data object ScreenButtonClicked : ScreenAction

    data object DialogButtonClicked : ScreenAction

    data object BottomSheetButtonClicked : ScreenAction

    data object ReplaceAllButtonClicked : ScreenAction

    data object ScreenForResultButtonClicked : ScreenAction

    data object LocationPermissionButtonClicked : ScreenAction
}

@Inject
class ScreenStateMachine(
    route: ScreenRoute,
    private val navigator: ScreenNavigator,
    private val locationPermissionNavigator: LocationPermissionNavigator,
) : StateMachine<ScreenState, ScreenAction> {
    override val state: Flow<ScreenState> = MutableStateFlow(ScreenState(route.number)).let { state ->
        channelFlow {
            launch {
                navigator.destinationResult.results.collect { result ->
                    state.update { it.copy(result = result.data) }
                }
            }
            launch {
                locationPermissionNavigator.locationPermissionGranted.collect { granted ->
                    state.update { it.copy(locationPermissionGranted = granted) }
                }
            }
            state.collect { send(it) }
        }
    }

    override suspend fun dispatch(action: ScreenAction) {
        when (action) {
            ScreenAction.ScreenButtonClicked -> navigator.navigateToScreen()
            ScreenAction.DialogButtonClicked -> navigator.navigateToDialog()
            ScreenAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
            ScreenAction.ReplaceAllButtonClicked -> navigator.replaceAllWithNewRoot()
            ScreenAction.ScreenForResultButtonClicked -> navigator.navigateToScreenForResult()
            ScreenAction.LocationPermissionButtonClicked -> locationPermissionNavigator.requestLocationPermission()
        }
    }
}
