package com.freeletics.khonshu.navigation

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Test

internal class TestHostNavigatorDestinationNavigatorTest {
    @Test
    fun `forward navigation is recorded`() = runTest {
        val hostNavigator = TestHostNavigator()
        val navigator = hostNavigator.destinationNavigator()

        hostNavigator.test {
            navigator.navigateTo(TestRoute(1))

            awaitNavigateTo(TestRoute(1))
        }
    }

    @Test
    fun `back navigation is recorded while the destination is current`() = runTest {
        val hostNavigator = TestHostNavigator()
        val navigator = hostNavigator.destinationNavigator()

        hostNavigator.test {
            navigator.navigateBack()
            navigator.navigateUp()
            navigator.navigateBackTo(TestRoute::class, inclusive = true)

            awaitNavigateBack()
            awaitNavigateUp()
            awaitNavigateBackTo(TestRoute::class, inclusive = true)
        }
    }

    @Test
    fun `navigate block is recorded while the destination is current`() = runTest {
        val hostNavigator = TestHostNavigator()
        val navigator = hostNavigator.destinationNavigator()

        hostNavigator.test {
            navigator.navigate {
                navigateBack()
                navigateTo(TestRoute(1))
            }

            awaitNavigate {
                navigateBack()
                navigateTo(TestRoute(1))
            }
        }
    }

    @Test
    fun `back navigation is ignored while the destination is not current`() = runTest {
        val hostNavigator = TestHostNavigator()
        val navigator = hostNavigator.destinationNavigator(isCurrentDestination = false)

        hostNavigator.test {
            navigator.navigateBack()
            navigator.navigateUp()
            navigator.navigateBackTo(TestRoute::class, inclusive = false)
            navigator.navigate { navigateBack() }

            // forward navigation is not guarded, so this is the only event that is expected
            navigator.navigateTo(TestRoute(1))
            awaitNavigateTo(TestRoute(1))
        }
    }

    @Serializable
    private data class TestRoute(val number: Int) : NavRoute
}
