package com.freeletics.khonshu.navigation

import android.os.Parcelable
import app.cash.turbine.Event
import app.cash.turbine.Turbine
import com.freeletics.khonshu.navigation.internal.ActivityEvent
import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass

internal sealed interface TestEvent

@Poko
internal class NavigateToEvent(
    private val route: NavRoute,
) : TestEvent

@Poko
internal class NavigateToRootEvent(
    private val root: NavRoot,
    private val restoreRootState: Boolean,
) : TestEvent

@Poko
internal class NavigateToActivityEvent(
    private val event: ActivityEvent.NavigateTo,
) : TestEvent

internal data object UpEvent : TestEvent

internal data object BackEvent : TestEvent

@Poko
internal class BackToEvent(
    private val popUpTo: KClass<out BaseRoute>,
    private val inclusive: Boolean,
) : TestEvent

@Poko
internal class ResetToRootEvent(
    private val root: NavRoot,
) : TestEvent

@Poko
internal class ReplaceAllEvent(
    private val root: NavRoot,
) : TestEvent

@Poko
internal class ActivityResultEvent<I>(
    private val event: ActivityEvent.NavigateForResult<I>,
) : TestEvent

@Poko
internal class DestinationResultEvent<O : Parcelable>(
    private val key: NavigationResultRequest.Key<O>,
    private val result: O,
) : TestEvent

@Poko
internal class BatchEvent(
    private val events: List<TestEvent>,
) : TestEvent

internal fun toTestEvent(event: ActivityEvent): TestEvent {
    return when (event) {
        is ActivityEvent.NavigateTo -> NavigateToActivityEvent(event)
        is ActivityEvent.NavigateForResult<*> -> ActivityResultEvent(event)
    }
}

internal fun Turbine<TestEvent>.toTestEvent(): BatchEvent {
    close()
    val events = buildList {
        do {
            val event = takeEvent()
            if (event is Event.Item) {
                add(event.value)
            }
        } while (event is Event.Item)
    }
    return BatchEvent(events)
}
