package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.DestinationNavigator
import com.freeletics.khonshu.navigation.HostNavigator

@InternalNavigationCodegenApi
public class DefaultDestinationNavigator(
    hostNavigator: HostNavigator,
) : DestinationNavigator(hostNavigator)
