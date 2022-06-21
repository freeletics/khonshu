package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.compositeDisposable
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.coroutineScope
import com.freeletics.mad.whetstone.codegen.util.coroutineScopeCancel
import com.freeletics.mad.whetstone.codegen.util.intoSet
import com.freeletics.mad.whetstone.codegen.util.mainScope
import com.freeletics.mad.whetstone.codegen.util.module
import com.freeletics.mad.whetstone.codegen.util.multibinds
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.provides
import com.freeletics.mad.whetstone.codegen.util.scopeToAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.Closeable

internal class NavEntrySubcomponentModuleGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

    private val moduleClassName = ClassName("NavEntry${data.baseName}Module")

    internal fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(moduleClassName)
            .addAnnotation(module)
            .addAnnotation(contributesToAnnotation(data.scope))
            .addFunction(multibindsFunction())
            .apply {
                val companionFunctions = mutableListOf<FunSpec>()
                if (data.rxJavaEnabled) {
                    companionFunctions += FunSpec.builder("provideCompositeDisposable")
                        .addAnnotation(provides)
                        .addAnnotation(scopeToAnnotation(data.scope))
                        .returns(compositeDisposable)
                        .addStatement("return %T()", compositeDisposable)
                        .build()

                    companionFunctions += FunSpec.builder("bindCompositeDisposable")
                        .addAnnotation(provides)
                        .addAnnotation(intoSet)
                        .addParameter(compositeDisposable.propertyName, compositeDisposable)
                        .returns(Closeable::class.asClassName())
                        .beginControlFlow("return %T", Closeable::class.asClassName())
                        .addStatement("%L.clear()", compositeDisposable.propertyName)
                        .endControlFlow()
                        .build()
                }
                if (data.coroutinesEnabled) {
                    companionFunctions += FunSpec.builder("provideCoroutineScope")
                        .addAnnotation(provides)
                        .addAnnotation(scopeToAnnotation(data.scope))
                        .returns(coroutineScope)
                        .addStatement("return %M()", mainScope)
                        .build()

                    companionFunctions += FunSpec.builder("bindCoroutineScope")
                        .addAnnotation(provides)
                        .addAnnotation(intoSet)
                        .addParameter(coroutineScope.propertyName, coroutineScope)
                        .returns(Closeable::class.asClassName())
                        .beginControlFlow("return %T", Closeable::class.asClassName())
                        .addStatement("%L.%M()", coroutineScope.propertyName, coroutineScopeCancel)
                        .endControlFlow()
                        .build()
                }

                if (companionFunctions.isNotEmpty()) {
                    addType(
                        TypeSpec.companionObjectBuilder()
                            .addFunctions(companionFunctions)
                            .build()
                    )
                }
            }
            .build()
    }

    private fun multibindsFunction(): FunSpec {
        return FunSpec.builder("bindCancellable")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds)
            .returns(SET.parameterizedBy(Closeable::class.asClassName()))
            .build()
    }
}
