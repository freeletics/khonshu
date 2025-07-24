package com.freeletics.khonshu.codegen.parser

import com.freeletics.flowredux2.LegacyFlowReduxStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
public abstract class TestStateMachine<S : Any, A : Any>(
    initial: S,
) : LegacyFlowReduxStateMachine<S, A>(initial)
