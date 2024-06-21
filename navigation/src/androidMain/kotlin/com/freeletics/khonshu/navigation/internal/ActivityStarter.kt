package com.freeletics.khonshu.navigation.internal

import android.content.ActivityNotFoundException
import android.content.Context
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.InternalActivityRoute
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.Navigator
import com.freeletics.khonshu.navigation.putRoute

internal class ActivityStarter(
    private val context: Context,
    private val navigator: Navigator,
) {
    fun start(route: ActivityRoute, fallbackRoute: NavRoute?) {
        val intent = route.buildIntent(context)
        if (route is InternalActivityRoute) {
            intent
                .setPackage(context.packageName)
                .putRoute(route)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            if (fallbackRoute != null) {
                navigator.navigateTo(fallbackRoute)
            } else {
                throw e
            }
        }
    }
}
