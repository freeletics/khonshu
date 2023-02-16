package com.freeletics.mad.navigator.compose.internal

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.compose.ActivityDestination
import com.freeletics.mad.navigator.compose.ContentDestination
import com.freeletics.mad.navigator.compose.NavDestination

@Composable
@Suppress("unused_parameter") //TODO
internal fun rememberNavigationExecutor(
    startRoot: BaseRoute,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
) : MultiStackNavigationExecutor {
    val context = LocalContext.current
    return remember {
        val activityStarter = { route: ActivityRoute, destination: ActivityDestination ->
            val intent = Intent(destination.intent)
            intent.fillIn(route.fillInIntent(), 0)
            context.startActivity(intent)
        }

        val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()
        val activityDestinations = destinations.filterIsInstance<ActivityDestination>()

        MultiStackNavigationExecutor(
            activityStarter,
            contentDestinations,
            activityDestinations,
        )
    }
}
