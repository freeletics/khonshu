package com.freeletics.mad.navigator.compose.internal

import android.content.Context
import android.content.Intent
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.compose.ActivityDestination
import com.freeletics.mad.navigator.internal.ActivityDestinationId

internal class ActivityStarter(
    private val context: Context,
    private val activityDestinations: List<ActivityDestination>,
) {
    fun start(route: ActivityRoute) {
        val destination = activityDestinations.find {
            it.id == ActivityDestinationId(route::class)
        }
        requireNotNull(destination) { "Did not find destination for $route" }
        val intent = Intent(destination.intent)
        intent.fillIn(route.fillInIntent(), 0)
        context.startActivity(intent)
    }
}
