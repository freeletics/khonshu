package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.anvilCompilation
import com.freeletics.khonshu.codegen.parser.allSuperTypes
import com.freeletics.khonshu.codegen.simpleCodeGenerator
import com.freeletics.khonshu.codegen.util.stateMachine
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import org.junit.Test

internal class AllSuperTypesTest {

    private fun Sequence<TypeName>.find(expected: TypeName): ParameterizedTypeName? {
        return firstNotNullOfOrNull {
            (it as? ParameterizedTypeName)?.takeIf { it.rawType == expected }
        }
    }

    @Test
    fun `type parameters are resolved`() {
        anvilCompilation(
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
            codeGenerators = listOf(
                simpleCodeGenerator { psiRef ->
                    when (psiRef.shortName) {
                        "StateMachine" -> {}
                        "Implementation" -> {
                            val superType = psiRef.allSuperTypes().find(
                                ClassName("com.freeletics.test", "StateMachine"),
                            )
                            assertThat(superType!!.typeArguments[0]).isEqualTo(STRING)
                            assertThat(superType.typeArguments[1]).isEqualTo(INT)
                        }
                        "StateMachineWithShortTypeParameters" -> {}
                        "ImplementationWithShortTypeParameters" -> {
                            val superType = psiRef.allSuperTypes().find(
                                ClassName("com.freeletics.test", "StateMachine"),
                            )
                            assertThat(superType!!.typeArguments[0]).isEqualTo(LONG)
                            assertThat(superType.typeArguments[1]).isEqualTo(BOOLEAN)
                        }
                        "StateMachineWithSwappedParameters" -> {}
                        "ImplementationWithWithSwappedParameters" -> {
                            val superType = psiRef.allSuperTypes().find(
                                ClassName("com.freeletics.test", "StateMachine"),
                            )
                            assertThat(superType!!.typeArguments[0]).isEqualTo(STRING)
                            assertThat(superType.typeArguments[1]).isEqualTo(INT)
                        }
                        "StateMachineWithExtraParameters" -> {}
                        "ImplementationWithWithExtraParameters" -> {
                            val superType = psiRef.allSuperTypes().find(
                                ClassName("com.freeletics.test", "StateMachine"),
                            )
                            assertThat(superType!!.typeArguments[0]).isEqualTo(SHORT)
                            assertThat(superType.typeArguments[1]).isEqualTo(STRING)
                        }
                        "Hierarchy1" -> {}
                        "Hierarchy2" -> {}
                        "HierarchyImplementation" -> {
                            val superType = psiRef.allSuperTypes().find(
                                ClassName("com.freeletics.test", "StateMachine"),
                            )
                            assertThat(superType!!.typeArguments[0]).isEqualTo(SHORT)
                            assertThat(superType.typeArguments[1]).isEqualTo(STRING)
                        }
                        else -> throw NotImplementedError(psiRef.shortName)
                    }
                },
            ),
        ).compile {
            assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        }
    }

    @Test
    fun `type parameters are resolved for external classes`() {
        anvilCompilation(
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
            codeGenerators = listOf(
                simpleCodeGenerator { psiRef ->
                    when (psiRef.shortName) {
                        "CreateCustomActivityStateMachine" -> {
                            val superType = psiRef.allSuperTypes().find(stateMachine)
                            assertThat(superType!!.typeArguments[0])
                                .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityState"))
                            assertThat(superType.typeArguments[1])
                                .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityAction"))
                        }
                        "CreateCustomActivityState",
                        "DefaultCreateCustomActivityLoadingState",
                        "CreateCustomActivityAction",
                        -> {}
                        else -> throw NotImplementedError(psiRef.shortName)
                    }
                },
            ),
        ).compile {
            assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        }
    }
}
