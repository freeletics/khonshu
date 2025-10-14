package com.freeletics.khonshu.codegen.parser

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
public abstract class TestStateMachine<S : Any, A : Any>(
    initial: S,
) : FlowReduxStateMachine<S, A>(initial)
