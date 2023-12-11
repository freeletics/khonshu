package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.FileGenerator
import com.freeletics.khonshu.codegen.parser.toComposeScreenDestinationData
import com.freeletics.khonshu.codegen.parser.toNavHostActivityData
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ksp.writeTo

public class KhonshuSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    @AutoService(SymbolProcessorProvider::class)
    public class KhonshuSymbolProcessorProvider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return KhonshuSymbolProcessor(environment.codeGenerator, environment.logger)
        }
    }

    private val fileGenerator = FileGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.generateCodeForAnnotation<NavDestination> {
            toComposeScreenDestinationData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<NavHostActivity> {
            toNavHostActivityData(it, resolver, logger)
        }
        return emptyList()
    }

    private inline fun <reified A : Annotation> Resolver.generateCodeForAnnotation(
        parser: KSFunctionDeclaration.(KSAnnotation) -> BaseData?,
    ) {
        getSymbolsWithAnnotation(A::class.qualifiedName!!)
            .forEach {
                if (it !is KSFunctionDeclaration) {
                    logger.error("@${A::class.simpleName} can only be applied to functions", it)
                    return@forEach
                }

                it.annotations.filter {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == A::class.qualifiedName
                }.forEach annotation@{ annotation ->
                    val data = parser(it, annotation) ?: return@annotation
                    val file = fileGenerator.generate(data)
                    file.writeTo(codeGenerator, aggregating = false, originatingKSFiles = listOf(it.containingFile!!))
                }
            }
    }
}
