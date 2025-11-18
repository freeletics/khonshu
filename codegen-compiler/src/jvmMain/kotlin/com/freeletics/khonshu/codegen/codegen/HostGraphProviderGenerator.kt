package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.util.getGraph
import com.freeletics.khonshu.codegen.util.globalGraphProvider
import com.freeletics.khonshu.codegen.util.hostGraphProvider
import com.freeletics.khonshu.codegen.util.intent
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.stackEntryStoreHolder
import com.freeletics.khonshu.codegen.util.viewModelStoreOwner
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
            .addAnnotation(optIn())
            .addSuperinterface(hostGraphProvider)
            .primaryConstructor(constructor())
            .addProperty(viewModelStoreOwnerProperty())
            .addProperty(globalGraphProviderProperty())
            .addProperty(stackEntryStoreHolderProperty())
            .addProperty(intentProperty())
            .addFunction(provideFunction())
            .build()
    }

    private fun constructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("viewModelStoreOwner", viewModelStoreOwner)
            .addParameter("globalGraphProvider", globalGraphProvider)
            .addParameter("stackEntryStoreHolder", stackEntryStoreHolder)
            .addParameter("intent", intent)
            .build()
    }

    private fun viewModelStoreOwnerProperty(): PropertySpec {
        return PropertySpec.builder("viewModelStoreOwner", viewModelStoreOwner)
            .addModifiers(PRIVATE)
            .initializer("viewModelStoreOwner")
            .build()
    }

    private fun intentProperty(): PropertySpec {
        return PropertySpec.builder("intent", intent)
            .addModifiers(PRIVATE)
            .initializer("intent")
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

    private fun provideFunction(): FunSpec {
        val typeVariable = TypeVariableName("C")
        return FunSpec.builder("provide")
            .addModifiers(OVERRIDE)
            .addTypeVariable(typeVariable)
            .addParameter("scope", KClass::class.asClassName().parameterizedBy(STAR))
            .returns(typeVariable)
            .beginControlFlow(
                "return %M(viewModelStoreOwner, globalGraphProvider, scope, %T::class, %T::class) { factory: %T, savedStateHandle ->",
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
