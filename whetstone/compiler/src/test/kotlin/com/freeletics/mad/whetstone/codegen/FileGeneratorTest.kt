package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.SimpleData
import com.squareup.kotlinpoet.ClassName

internal val full = SimpleData(
    baseName = "Test",
    packageName = "com.test",
    scope = ClassName("com.test", "TestScreen"),
    parentScope = ClassName("com.test.parent", "TestParentScope"),
    dependencies = ClassName("com.test", "TestDependencies"),
    stateMachine = ClassName("com.test", "TestStateMachine"),
    navigation = CommonData.Navigation(
        navigator = ClassName("com.test", "TestNavigator"),
        navigationHandler = ClassName("com.test.navigation", "TestNavigationHandler"),
    ),
    coroutinesEnabled = true,
    rxJavaEnabled = true,
)
