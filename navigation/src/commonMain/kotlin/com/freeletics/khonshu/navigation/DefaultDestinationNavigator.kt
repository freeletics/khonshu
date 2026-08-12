package com.freeletics.khonshu.navigation

import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi

@InternalNavigationCodegenApi
public class DefaultDestinationNavigator(
    hostNavigator: HostNavigator,
) : DestinationNavigator(hostNavigator)
