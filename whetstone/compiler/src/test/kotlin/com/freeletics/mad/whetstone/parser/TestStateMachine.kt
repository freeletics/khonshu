package com.freeletics.mad.whetstone.parser

import com.freeletics.flowredux.dsl.FlowReduxStateMachine

abstract class TestStateMachine<S : Any, A : Any>(
    initial: S
) : FlowReduxStateMachine<S, A>(initial)
