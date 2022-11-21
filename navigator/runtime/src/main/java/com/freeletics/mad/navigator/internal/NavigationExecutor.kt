package com.freeletics.mad.navigator.internal

import android.os.Parcelable
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.NavigationResultRequest
import kotlin.reflect.KClass

@InternalNavigatorApi
public interface NavigationExecutor {

    public fun navigate(route: NavRoute)
    public fun navigate(root: NavRoot, restoreRootState: Boolean)
    public fun navigate(route: ActivityRoute)
    public fun navigateUp()
    public fun navigateBack()
    public fun navigateBackTo(route: KClass<out BaseRoute>, isInclusive: Boolean)
    public fun deliverResult(key: NavigationResultRequest.Key<*>, result: Parcelable)
}
