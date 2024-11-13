#!/usr/bin/env kotlin

@file:DependsOn("com.freeletics.gradle:scripts-formatting:0.17.1")

import com.freeletics.gradle.scripts.KtLintCli
import com.github.ajalt.clikt.core.main

KtLintCli().main(args)
