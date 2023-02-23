package com.freeletics.mad.whetstone.parser

import com.google.common.truth.Truth.assertThat
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.squareup.anvil.compiler.internal.testing.simpleCodeGenerator
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.Result
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.name.FqName
import org.junit.Test

@OptIn(ExperimentalAnvilApi::class, ExperimentalCompilerApi::class)
class ReferenceTest {

    @Test
    fun `inner classes are parsed`() {
        compile(
            """
            package com.freeletics.test
              
            interface StateMachine<State, Action>
            class Implementation : StateMachine<String, Int>
            
            interface StateMachineWithShortTypeParameters<S, A> : StateMachine<S, A>
            class ImplementationWithShortTypeParameters : StateMachineWithShortTypeParameters<Long, Boolean>
            
            interface StateMachineWithSwappedParameters<A2, S2> : StateMachine<S2, A2>
            class ImplementationWithWithSwappedParameters : StateMachineWithSwappedParameters<Int, String>
            
            interface StateMachineWithExtraParameters<T1, T2, S3, A3, T3> : StateMachine<S3, A3>
            class ImplementationWithWithExtraParameters : StateMachineWithExtraParameters<Boolean, Long, Short, String, Int>
    
            abstract class Hierarchy1<A4, S4>: StateMachineWithShortTypeParameters<S4, A4>
            abstract class Hierarchy2<T1, T2, S5, A5, T3>: Hierarchy1<A5, S5>()
            class HierarchyImplementation : Hierarchy2<Boolean, Long, Short, String, Int>()

            """.trimIndent(),
            allWarningsAsErrors = false,
            codeGenerators = listOf(
                simpleCodeGenerator { psiRef ->
                    when (psiRef.shortName) {
                        "StateMachine" -> {}
                        "Implementation" -> {
                            val superType = psiRef.superTypeReference(FqName("com.freeletics.test.StateMachine"))
                            assertThat(psiRef.resolveTypeParameter("State", superType)).isEqualTo(STRING)
                            assertThat(psiRef.resolveTypeParameter("Action", superType)).isEqualTo(INT)
                        }
                        "StateMachineWithShortTypeParameters" -> {}
                        "ImplementationWithShortTypeParameters" -> {
                            val superType = psiRef.superTypeReference(FqName("com.freeletics.test.StateMachine"))
                            assertThat(psiRef.resolveTypeParameter("State", superType)).isEqualTo(LONG)
                            assertThat(psiRef.resolveTypeParameter("Action", superType)).isEqualTo(BOOLEAN)
                        }
                        "StateMachineWithSwappedParameters" -> {}
                        "ImplementationWithWithSwappedParameters" -> {
                            val superType = psiRef.superTypeReference(FqName("com.freeletics.test.StateMachine"))
                            assertThat(psiRef.resolveTypeParameter("State", superType)).isEqualTo(STRING)
                            assertThat(psiRef.resolveTypeParameter("Action", superType)).isEqualTo(INT)
                        }
                        "StateMachineWithExtraParameters" -> {}
                        "ImplementationWithWithExtraParameters" -> {
                            val superType = psiRef.superTypeReference(FqName("com.freeletics.test.StateMachine"))
                            assertThat(psiRef.resolveTypeParameter("State", superType)).isEqualTo(SHORT)
                            assertThat(psiRef.resolveTypeParameter("Action", superType)).isEqualTo(STRING)
                        }
                        "Hierarchy1" -> {}
                        "Hierarchy2" -> {}
                        "HierarchyImplementation" -> {
                            val superType = psiRef.superTypeReference(FqName("com.freeletics.test.StateMachine"))
                            assertThat(psiRef.resolveTypeParameter("State", superType)).isEqualTo(SHORT)
                            assertThat(psiRef.resolveTypeParameter("Action", superType)).isEqualTo(STRING)
                        }
                        else -> throw NotImplementedError(psiRef.shortName)
                    }

                    null
                }
            )
        ) {
            assertThat(exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        }
    }

    private fun compile(
        @Language("kotlin") vararg sources: String,
        previousCompilationResult: Result? = null,
        enableDaggerAnnotationProcessor: Boolean = false,
        codeGenerators: List<CodeGenerator> = emptyList(),
        allWarningsAsErrors: Boolean = true,
        block: Result.() -> Unit = { }
    ): Result {
        return compileAnvil(
            sources = sources,
            useIR = true,
            allWarningsAsErrors = allWarningsAsErrors,
            previousCompilationResult = previousCompilationResult,
            enableDaggerAnnotationProcessor = enableDaggerAnnotationProcessor,
            codeGenerators = codeGenerators,
            block = block
        )
    }
}
