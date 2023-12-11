package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.util.deepLinkHandler
import com.freeletics.khonshu.codegen.util.deepLinkPrefix
import com.freeletics.khonshu.codegen.util.immutableSet
import com.freeletics.khonshu.codegen.util.jvmSuppressWildcards
import com.freeletics.khonshu.codegen.util.module
import com.freeletics.khonshu.codegen.util.multibinds
import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.provides
import com.freeletics.khonshu.codegen.util.toImmutableSet
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec

internal class ActivityModuleGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    private val moduleClassName = ClassName("Khonshu${data.baseName}ActivityModule")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(bindDeepLinkHandlerFunction())
            .addFunction(bindDeepLinkPrefixFunction())
            .addType(companionObject())
            .build()
    }

    private fun bindDeepLinkHandlerFunction(): FunSpec {
        return FunSpec.builder("bindDeepLinkHandler")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .returns(SET.parameterizedBy(deepLinkHandler))
            .build()
    }

    private fun bindDeepLinkPrefixFunction(): FunSpec {
        return FunSpec.builder("bindDeepLinkPrefix")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .returns(SET.parameterizedBy(deepLinkPrefix))
            .build()
    }

    private fun companionObject(): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addFunction(provideImmutableNavDestinationsFunction())
            .addFunction(provideImmutableDeepLinkHandlersFunction())
            .addFunction(provideImmutableDeepLinkHandlerPrefixesFunction())
            .build()
    }

    private fun provideImmutableNavDestinationsFunction(): FunSpec {
        return FunSpec.builder("provideImmutableNavDestinations")
            .addAnnotation(provides)
            .addParameter("destinations", SET.parameterizedBy(navigationDestination).jvmSuppressWildcards())
            .returns(immutableSet.parameterizedBy(navigationDestination))
            .addStatement("return destinations.%M()", toImmutableSet)
            .build()
    }

    private fun provideImmutableDeepLinkHandlersFunction(): FunSpec {
        return FunSpec.builder("provideImmutableDeepLinkHandlers")
            .addAnnotation(provides)
            .addParameter("handlers", SET.parameterizedBy(deepLinkHandler).jvmSuppressWildcards())
            .returns(immutableSet.parameterizedBy(deepLinkHandler))
            .addStatement("return handlers.%M()", toImmutableSet)
            .build()
    }

    private fun provideImmutableDeepLinkHandlerPrefixesFunction(): FunSpec {
        return FunSpec.builder("provideImmutableDeepLinkPrefixes")
            .addAnnotation(provides)
            .addParameter("prefixes", SET.parameterizedBy(deepLinkPrefix).jvmSuppressWildcards())
            .returns(immutableSet.parameterizedBy(deepLinkPrefix))
            .addStatement("return prefixes.%M()", toImmutableSet)
            .build()
    }
}
