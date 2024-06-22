package com.freeletics.khonshu.navigation

import android.os.Parcelable
import app.cash.turbine.Event
import app.cash.turbine.Turbine
import com.freeletics.khonshu.navigation.internal.ActivityEvent
import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass

internal sealed interface TestNavEvent

@Poko
internal class NavigateToEvent(
    private val route: NavRoute,
) : TestNavEvent

@Poko
internal class NavigateToRootEvent(
    private val root: NavRoot,
    private val restoreRootState: Boolean,
) : TestNavEvent

@Poko
internal class NavigateToActivityEvent(
    private val event: ActivityEvent.NavigateTo
) : TestNavEvent

internal data object UpEvent : TestNavEvent

internal data object BackEvent : TestNavEvent

@Poko
internal class BackToEvent(
    private val popUpTo: KClass<out BaseRoute>,
    private val inclusive: Boolean,
) : TestNavEvent

@Poko
internal class ResetToRoot(
    private val root: NavRoot,
) : TestNavEvent

@Poko
internal class ReplaceAll(
    private val root: NavRoot,
) : TestNavEvent

@Poko
internal class ActivityResultEvent<I>(
    private val event: ActivityEvent.NavigateForResult<I>
) : TestNavEvent

@Poko
internal class DestinationResultEvent<O : Parcelable>(
    private val key: NavigationResultRequest.Key<O>,
    private val result: O,
) : TestNavEvent

@Poko
internal class MultiNavEvent(
    private val navEvents: List<TestNavEvent>,
) : TestNavEvent

internal fun fromNavEvent(event: ActivityEvent): TestNavEvent {
    return when(event) {
        is ActivityEvent.NavigateTo -> NavigateToActivityEvent(event)
        is ActivityEvent.NavigateForResult<*> -> ActivityResultEvent(event)
    }
}

internal fun Turbine<TestNavEvent>.toTestNavEvent(): MultiNavEvent {
    close()
    val events = buildList {
        do {
            val event = takeEvent()
            if (event is Event.Item) {
                add(event.value)
            }
        } while (event is Event.Item)
    }
    return MultiNavEvent(events)
}
