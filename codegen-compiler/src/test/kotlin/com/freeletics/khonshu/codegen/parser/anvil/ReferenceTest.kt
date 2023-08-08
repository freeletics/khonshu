package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.compileAnvil
import com.google.common.truth.Truth.assertThat
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.internal.testing.simpleCodeGenerator
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.name.FqName
import org.junit.Test

internal class ReferenceTest {

    @Test
    fun `type parameters are resolved`() {
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
                },
            ),
        ) {
            assertThat(exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        }
    }

    @Test
    fun `type parameters are resolved for external classes`() {
        compile(
            """
                package com.freeletics.test

                import com.freeletics.khonshu.codegen.parser.TestStateMachine

                class CreateCustomActivityStateMachine :
                    TestStateMachine<CreateCustomActivityState, CreateCustomActivityAction>(
                        DefaultCreateCustomActivityLoadingState,
                    )

                sealed interface CreateCustomActivityState
                object DefaultCreateCustomActivityLoadingState : CreateCustomActivityState
                sealed interface CreateCustomActivityAction
            """.trimIndent(),
            allWarningsAsErrors = false,
            codeGenerators = listOf(
                simpleCodeGenerator { psiRef ->
                    when (psiRef.shortName) {
                        "CreateCustomActivityStateMachine" -> {
                            val superType = psiRef.superTypeReference(
                                FqName("com.freeletics.khonshu.statemachine.StateMachine"),
                            )
                            assertThat(psiRef.resolveTypeParameter("State", superType))
                                .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityState"))
                            assertThat(psiRef.resolveTypeParameter("Action", superType))
                                .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityAction"))
                        }
                        "CreateCustomActivityState",
                        "DefaultCreateCustomActivityLoadingState",
                        "CreateCustomActivityAction",
                        -> {}
                        else -> throw NotImplementedError(psiRef.shortName)
                    }

                    null
                },
            ),
        ) {
            assertThat(exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        }
    }

    private fun compile(
        @Language("kotlin") vararg sources: String,
        previousCompilationResult: JvmCompilationResult? = null,
        enableDaggerAnnotationProcessor: Boolean = false,
        codeGenerators: List<CodeGenerator> = emptyList(),
        allWarningsAsErrors: Boolean = true,
        block: JvmCompilationResult.() -> Unit = { },
    ): JvmCompilationResult {
        return compileAnvil(
            sources = sources,
            allWarningsAsErrors = allWarningsAsErrors,
            previousCompilationResult = previousCompilationResult,
            enableDaggerAnnotationProcessor = enableDaggerAnnotationProcessor,
            codeGenerators = codeGenerators,
            block = block,
        )
    }
}
