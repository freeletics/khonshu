package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.util.InternalCodegenApi
import com.freeletics.khonshu.codegen.util.getGraph
import com.freeletics.khonshu.codegen.util.globalGraphProvider
import com.freeletics.khonshu.codegen.util.hostGraphProvider
import com.freeletics.khonshu.codegen.util.intent
import com.freeletics.khonshu.codegen.util.internalNavigatorApi
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.stackEntry
import com.freeletics.khonshu.codegen.util.stackEntryStoreHolder
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal class HostGraphProviderGenerator(
    override val data: HostActivityData,
) : Generator<HostActivityData>() {
    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(graphProviderClassName)
            .addAnnotation(optIn(InternalCodegenApi, internalNavigatorApi))
            .addSuperinterface(hostGraphProvider)
            .primaryConstructor(constructor())
            .addProperty(globalGraphProviderProperty())
            .addProperty(stackEntryStoreHolderProperty())
            .addProperty(savedStateHandleProperty())
            .addProperty(intentProperty())
            .addFunction(provideFunction())
            .build()
    }

    private fun constructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("globalGraphProvider", globalGraphProvider)
            .addParameter("stackEntryStoreHolder", stackEntryStoreHolder)
            .addParameter("savedStateHandle", savedStateHandle)
            .addParameter("intent", intent)
            .build()
    }

    private fun globalGraphProviderProperty(): PropertySpec {
        return PropertySpec.builder("globalGraphProvider", globalGraphProvider)
            .addModifiers(PRIVATE)
            .initializer("globalGraphProvider")
            .build()
    }

    private fun stackEntryStoreHolderProperty(): PropertySpec {
        return PropertySpec.builder("stackEntryStoreHolder", stackEntryStoreHolder)
            .addModifiers(PRIVATE)
            .initializer("stackEntryStoreHolder")
            .build()
    }

    private fun savedStateHandleProperty(): PropertySpec {
        return PropertySpec.builder("savedStateHandle", savedStateHandle)
            .addModifiers(PRIVATE)
            .initializer("savedStateHandle")
            .build()
    }

    private fun intentProperty(): PropertySpec {
        return PropertySpec.builder("intent", intent)
            .addModifiers(PRIVATE)
            .initializer("intent")
            .build()
    }

    private fun provideFunction(): FunSpec {
        val typeVariable = TypeVariableName("C")
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addTypeVariable(typeVariable)
            .addParameter("scope", KClass::class.asClassName().parameterizedBy(STAR))
            .returns(typeVariable)
            .addStatement(
                "val stackEntryStore = stackEntryStoreHolder.provideStore(%T.Id(%S))",
                stackEntry,
                data.baseName,
            )
            .beginControlFlow(
                "return %M(stackEntryStore, globalGraphProvider, scope, %T::class, %T::class) { factory: %T ->",
                getGraph,
                data.scope,
                data.parentScope,
                graphFactoryClassName,
            )
            .addStatement(
                "factory.%L(stackEntryStoreHolder, savedStateHandle, intent)",
                graphFactoryCreateFunctionName,
            )
            .endControlFlow()
            .build()
    }
}
