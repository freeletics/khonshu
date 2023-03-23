package com.freeletics.mad.whetstone.parser

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
public abstract class TestStateMachine<S : Any, A : Any>(
    initial: S
) : FlowReduxStateMachine<S, A>(initial)
