package com.freeletics.khonshu.navigation

import android.content.Intent
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow

public class TestHostNavigator(
    public var handleDeepLinkRoute: NavRoute? = null,
) : HostNavigator() {

    internal val navEventNavigator = NavEventNavigator()

    @InternalNavigationCodegenApi
    @InternalNavigationTestingApi
    override val snapshot: State<StackSnapshot>
        get() = throw UnsupportedOperationException()

    @InternalNavigationTestingApi
    override val onBackPressedCallback: OnBackPressedCallback
        get() = throw UnsupportedOperationException()

    /**
     * The fake implementation will call [navigateTo] with [handleDeepLinkRoute] if
     * the latter is not `null`. Otherwise it will just return false.
     */
    override fun handleDeepLink(
        intent: Intent,
        deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
        deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
    ): Boolean {
        handleDeepLinkRoute?.let {
            navEventNavigator.navigateTo(it)
            return true
        }
        return false
    }

    override fun navigate(block: Navigator.() -> Unit) {
        navEventNavigator.navigate(block)
    }

    override fun navigateTo(route: NavRoute) {
        navEventNavigator.navigateTo(route)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        navEventNavigator.navigateToRoot(root, restoreRootState)
    }

    override fun navigateUp() {
        navEventNavigator.navigateUp()
    }

    override fun navigateBack() {
        navEventNavigator.navigateBack()
    }

    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        navEventNavigator.navigateBackTo(popUpTo, inclusive)
    }

    override fun resetToRoot(root: NavRoot) {
        navEventNavigator.resetToRoot(root)
    }

    override fun replaceAll(root: NavRoot) {
        navEventNavigator.replaceAll(root)
    }

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        navEventNavigator.deliverNavigationResult(key, result)
    }

    @InternalNavigationApi
    @InternalNavigationCodegenApi
    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        return navEventNavigator.registerForNavigationResultInternal(id, resultType)
    }

    override fun backPresses(): Flow<Unit> {
        return navEventNavigator.backPresses()
    }

    override fun <T> backPresses(value: T): Flow<T> {
        return navEventNavigator.backPresses(value)
    }
}
