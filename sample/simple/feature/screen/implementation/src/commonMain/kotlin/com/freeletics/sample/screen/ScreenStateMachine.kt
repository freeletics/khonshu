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
    private val _state = MutableStateFlow(ScreenState(route.number))
    override val state: Flow<ScreenState> = channelFlow {
        launch { observeScreenResults() }
        launch { observeLocationPermissionResults() }
        _state.collect { send(it) }
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

    private suspend fun observeScreenResults() {
        navigator.destinationResult.results.collect { result ->
            _state.update { it.copy(result = result.data) }
        }
    }

    private suspend fun observeLocationPermissionResults() {
        locationPermissionNavigator.locationPermissionGranted.collect { granted ->
            _state.update { it.copy(locationPermissionGranted = granted) }
        }
    }
}
