package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel

internal sealed interface TestEvent

@Poko
internal class NavigateToEvent(
    private val route: NavRoute,
) : TestEvent

internal data object UpEvent : TestEvent

internal data object BackEvent : TestEvent

@Poko
internal class BackToEvent(
    private val popUpTo: KClass<out BaseRoute>,
    private val inclusive: Boolean,
) : TestEvent

@Poko
internal class SwitchBackStackEvent(
    private val root: NavRoot,
) : TestEvent

@Poko
internal class ShowRootEvent(
    private val root: NavRoot,
) : TestEvent

@Poko
internal class ReplaceAllBackStacksEvent(
    private val root: NavRoot,
) : TestEvent

@Poko
internal class NavigateToActivityEvent(
    private val event: ActivityEvent.NavigateTo,
) : TestEvent

@Poko
internal class ActivityResultEvent<I>(
    private val event: ActivityEvent.NavigateForResult<I>,
) : TestEvent

@Poko
internal class DestinationResultEvent<O>(
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

internal fun Channel<TestEvent>.toTestEvent(): BatchEvent {
    val events = buildList {
        do {
            val event = tryReceive().getOrNull()
            if (event != null) {
                add(event)
            }
        } while (event != null)
    }
    return BatchEvent(events)
}
