package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.FileGenerator
import com.freeletics.khonshu.codegen.compose.ComposeDestination
import com.freeletics.khonshu.codegen.compose.ComposeScreen
import com.freeletics.khonshu.codegen.compose.NavHostActivity
import com.freeletics.khonshu.codegen.fragment.ComposeDestination as ComposeFragmentDestination
import com.freeletics.khonshu.codegen.fragment.ComposeFragment
import com.freeletics.khonshu.codegen.fragment.RendererDestination
import com.freeletics.khonshu.codegen.fragment.RendererFragment
import com.freeletics.khonshu.codegen.parser.ksp.toComposeFragmentData
import com.freeletics.khonshu.codegen.parser.ksp.toComposeFragmentDestinationData
import com.freeletics.khonshu.codegen.parser.ksp.toComposeScreenData
import com.freeletics.khonshu.codegen.parser.ksp.toComposeScreenDestinationData
import com.freeletics.khonshu.codegen.parser.ksp.toNavHostActivityData
import com.freeletics.khonshu.codegen.parser.ksp.toRendererFragmentData
import com.freeletics.khonshu.codegen.parser.ksp.toRendererFragmentDestinationData
import com.google.auto.service.AutoService
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
        resolver.generateCodeForAnnotation<KSFunctionDeclaration, ComposeScreen> {
            toComposeScreenData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<KSFunctionDeclaration, ComposeDestination> {
            toComposeScreenDestinationData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<KSClassDeclaration, RendererFragment> {
            toRendererFragmentData(it, logger)
        }
        resolver.generateCodeForAnnotation<KSClassDeclaration, RendererDestination> {
            toRendererFragmentDestinationData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<KSFunctionDeclaration, ComposeFragment> {
            toComposeFragmentData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<KSFunctionDeclaration, ComposeFragmentDestination> {
            toComposeFragmentDestinationData(it, resolver, logger)
        }
        resolver.generateCodeForAnnotation<KSFunctionDeclaration, NavHostActivity> {
            toNavHostActivityData(it, resolver, logger)
        }
        return emptyList()
    }

    private inline fun <reified T : KSAnnotated, reified A : Annotation> Resolver.generateCodeForAnnotation(
        parser: T.(KSAnnotation) -> BaseData?,
    ) {
        getSymbolsWithAnnotation(A::class.qualifiedName!!)
            .forEach {
                if (it !is T) {
                    val target = when (T::class) {
                        KSFunctionDeclaration::class -> "functions"
                        KSClassDeclaration::class -> "classes"
                        else -> throw IllegalStateException("Khonshu internal error: unexpected class ${T::class}")
                    }
                    logger.error("@${A::class.simpleName} can only be applied to $target", it)
                    return@forEach
                }

                val annotation = it.annotations.first {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == A::class.qualifiedName
                }

                val data = parser(it, annotation) ?: return@forEach
                val file = fileGenerator.generate(data)
                file.writeTo(codeGenerator, aggregating = false, originatingKSFiles = listOf(it.containingFile!!))
            }
    }
}
