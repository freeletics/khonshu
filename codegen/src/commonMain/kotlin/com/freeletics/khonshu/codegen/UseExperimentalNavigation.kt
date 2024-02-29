package com.freeletics.khonshu.codegen

import javax.inject.Qualifier

/**
 * Qualifier used to provide a boolean that indicated whether experimental navigation should be
 * enabled or not. Only considered if [NavHostActivity.experimentalNavigation] is `true`.
 */
@Qualifier
public annotation class UseExperimentalNavigation
