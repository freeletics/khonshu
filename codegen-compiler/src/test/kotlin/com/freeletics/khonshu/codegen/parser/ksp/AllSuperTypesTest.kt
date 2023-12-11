package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.kspCompilation
import com.freeletics.khonshu.codegen.parser.allSuperTypes
import com.freeletics.khonshu.codegen.simpleSymbolProcessor
import com.freeletics.khonshu.codegen.util.stateMachine
import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.getClassDeclarationByName
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
        kspCompilation(
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
            symbolProcessors = listOf(
                simpleSymbolProcessor { resolver ->
                    resolver.getClassDeclarationByName("com.freeletics.test.Implementation")!!.also {
                        val superType = it.allSuperTypes(true).find(ClassName("com.freeletics.test", "StateMachine"))
                        assertThat(superType).isNotNull()
                        assertThat(superType!!.typeArguments[0]).isEqualTo(STRING)
                        assertThat(superType.typeArguments[1]).isEqualTo(INT)
                    }
                    resolver.getClassDeclarationByName(
                        "com.freeletics.test.ImplementationWithShortTypeParameters",
                    )!!.also {
                        val superType = it.allSuperTypes(true).find(ClassName("com.freeletics.test", "StateMachine"))
                        assertThat(superType).isNotNull()
                        assertThat(superType!!.typeArguments[0]).isEqualTo(LONG)
                        assertThat(superType.typeArguments[1]).isEqualTo(BOOLEAN)
                    }
                    resolver.getClassDeclarationByName(
                        "com.freeletics.test.ImplementationWithWithSwappedParameters",
                    )!!.also {
                        val superType = it.allSuperTypes(true).find(ClassName("com.freeletics.test", "StateMachine"))
                        assertThat(superType).isNotNull()
                        assertThat(superType!!.typeArguments[0]).isEqualTo(STRING)
                        assertThat(superType.typeArguments[1]).isEqualTo(INT)
                    }
                    resolver.getClassDeclarationByName(
                        "com.freeletics.test.ImplementationWithWithExtraParameters",
                    )!!.also {
                        val superType = it.allSuperTypes(true).find(ClassName("com.freeletics.test", "StateMachine"))
                        assertThat(superType).isNotNull()
                        assertThat(superType!!.typeArguments[0]).isEqualTo(SHORT)
                        assertThat(superType.typeArguments[1]).isEqualTo(STRING)
                    }
                    resolver.getClassDeclarationByName("com.freeletics.test.HierarchyImplementation")!!.also {
                        val superType = it.allSuperTypes(true).find(ClassName("com.freeletics.test", "StateMachine"))
                        assertThat(superType).isNotNull()
                        assertThat(superType!!.typeArguments[0]).isEqualTo(SHORT)
                        assertThat(superType.typeArguments[1]).isEqualTo(STRING)
                    }
                },
            ),
        ).compile {
            assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        }
    }

    @Test
    fun `type parameters are resolved for external classes`() {
        kspCompilation(
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
            symbolProcessors = listOf(
                simpleSymbolProcessor { resolver ->
                    resolver.getClassDeclarationByName("com.freeletics.test.CreateCustomActivityStateMachine")!!.also {
                        val superType = it.allSuperTypes(true).find(stateMachine)
                        assertThat(superType!!.typeArguments[0])
                            .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityState"))
                        assertThat(superType.typeArguments[1])
                            .isEqualTo(ClassName("com.freeletics.test", "CreateCustomActivityAction"))
                    }
                },
            ),
        ).compile {
            assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        }
    }
}
