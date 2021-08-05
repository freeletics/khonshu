package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.FileGenerator
import com.freeletics.mad.whetstone.codegen.composeFqName
import com.freeletics.mad.whetstone.codegen.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.emptyNavigationHandler
import com.freeletics.mad.whetstone.codegen.emptyNavigator
import com.freeletics.mad.whetstone.codegen.rendererFragmentFqName
import com.freeletics.mad.whetstone.codegen.retainedComponentFqName
import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.classesAndInnerClass
import com.squareup.anvil.compiler.internal.findAnnotation
import com.squareup.anvil.compiler.internal.findAnnotationArgument
import com.squareup.anvil.compiler.internal.requireFqName
import com.squareup.kotlinpoet.ClassName
import java.io.File
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

@OptIn(ExperimentalAnvilApi::class)
@AutoService(CodeGenerator::class)
class WhetstoneCodeGenerator : CodeGenerator {

    override fun isApplicable(context: AnvilContext): Boolean = !context.disableComponentMerging

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): Collection<GeneratedFile> {
        val classes = projectFiles
            .classesAndInnerClass(module)
            .mapNotNull { clazz -> generateCode(codeGenDir, module, clazz) }

        val functions = projectFiles
            .flatMap { it.declarations.filterIsInstance<KtNamedFunction>() }
            .mapNotNull { function -> generateCode(codeGenDir, module, function) }

        return classes.toList() + functions
    }

    private fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val component = declaration.findAnnotation(retainedComponentFqName, module) ?: return null
        var whetstone = component.toScreenData(declaration, module)
        //TODO check that navigationHandler type fits to extra (fragment vs no fragment)

        val compose = declaration.findAnnotation(composeFqName, module)
        if (compose != null) {
            whetstone = whetstone.copy(extra = Extra.Compose(withFragment = false))
        }

        val composeFragment = declaration.findAnnotation(composeFragmentFqName, module)
        if (composeFragment != null) {
            whetstone = whetstone.copy(extra = Extra.Compose(withFragment = true))
        }

        val renderer = declaration.findAnnotation(rendererFragmentFqName, module)
        if (renderer != null) {
            val factory = renderer.requireClassArgument("rendererFactory", 0, module)
            whetstone = whetstone.copy(extra = Extra.Renderer(factory))
        }

        val file = FileGenerator(whetstone).generate()
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun KtAnnotationEntry.toScreenData(
        declaration: KtDeclaration,
        module: ModuleDescriptor
    ): Data {
        return Data(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = requireClassArgument("scope", 0, module),
            parentScope = requireClassArgument("parentScope", 1, module),
            dependencies = requireClassArgument("dependencies", 2, module),
            stateMachine = requireClassArgument("stateMachine", 3, module),
            navigation = toNavigation(module),
            coroutinesEnabled = optionalBooleanArgument("coroutinesEnabled", 6) ?: false,
            rxJavaEnabled = optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
            extra = null
        )
    }

    private fun KtAnnotationEntry.toNavigation(
        module: ModuleDescriptor
    ): Navigation? {
        val navigator = optionalClassArgument("navigator", 4, module)
        val navigationHandler = optionalClassArgument("navigationHandler", 5, module)

        if (navigator != null && navigationHandler != null &&
            navigator != emptyNavigator && navigationHandler != emptyNavigationHandler) {
            return Navigation(navigator, navigationHandler)
        }
        if (navigator == null && navigationHandler == null) {
            return null
        }
        if (navigator == emptyNavigator && navigationHandler == emptyNavigationHandler) {
            return null
        }

        throw IllegalStateException("navigator and navigationHandler need to be set together")
    }

    private fun KtAnnotationEntry.requireClassArgument(
        name: String,
        index: Int,
        module: ModuleDescriptor
    ): ClassName {
        val classLiteralExpression = findAnnotationArgument<KtClassLiteralExpression>(name, index)
        if (classLiteralExpression != null) {
            return classLiteralExpression.requireFqName(module).asClassName(module)
        }
        throw AnvilCompilationException(
            "Couldn't find $name for ${requireFqName(module)}",
            element = this
        )
    }

    //TODO replace with a way to get default value
    private fun KtAnnotationEntry.optionalClassArgument(
        name: String,
        index: Int,
        module: ModuleDescriptor
    ): ClassName? {
        val classLiteralExpression = findAnnotationArgument<KtClassLiteralExpression>(name, index)
        if (classLiteralExpression != null) {
            return classLiteralExpression.requireFqName(module).asClassName(module)
        }
        return null
    }

    //TODO replace with a way to get default value
    private fun KtAnnotationEntry.optionalBooleanArgument(
        name: String,
        index: Int,
    ): Boolean? {
        val boolean = findAnnotationArgument<KtConstantExpression>(name, index)
        if (boolean != null) {
            return boolean.node.firstChildNode.text.toBoolean()
        }
        return null
    }
}
