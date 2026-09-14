package com.freeletics.sample.screen

import com.freeletics.khonshu.statemachine.StateMachine
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

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
    override val state: Flow<ScreenState> =
        merge(_state, observeScreenResults(), observeLocationPermissionResults())

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

    private fun observeScreenResults() = navigator.destinationResult.results
        .map {
            val currentState = _state.value
            currentState.copy(result = it.data)
        }

    private fun observeLocationPermissionResults() = locationPermissionNavigator.locationPermissionGranted
        .map { granted ->
            val currentState = _state.value
            currentState.copy(locationPermissionGranted = granted)
        }
}
