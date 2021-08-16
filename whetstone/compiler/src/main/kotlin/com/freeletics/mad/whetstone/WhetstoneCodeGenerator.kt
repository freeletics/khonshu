package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.FileGenerator
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.emptyNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.emptyNavigator
import com.freeletics.mad.whetstone.codegen.util.moduleFqName
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentFqName
import com.freeletics.mad.whetstone.codegen.util.rendererFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.retainedComponentFqName
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
        val rendererFragment = projectFiles
            .classesAndInnerClass(module)
            .mapNotNull { clazz -> generateRendererCode(codeGenDir, module, clazz) }

        val composeFragment = projectFiles
            .flatMap { it.declarations.filterIsInstance<KtNamedFunction>() }
            .mapNotNull { function -> generateComposeScreenCode(codeGenDir, module, function) }

        val composeScreen = projectFiles
            .flatMap { it.declarations.filterIsInstance<KtNamedFunction>() }
            .mapNotNull { function -> generateComposeFragmentCode(codeGenDir, module, function) }

        val navEntry = projectFiles
            .classesAndInnerClass(module)
            .filter { it.findAnnotation(moduleFqName, module) != null}
            .flatMap { it.declarations }
            .mapNotNull { clazz -> generateNavEntryCode(codeGenDir, module, clazz) }

        return rendererFragment.toList() + composeFragment + composeScreen + navEntry
    }

    private fun generateRendererCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val renderer = declaration.findAnnotation(rendererFragmentFqName, module) ?: return null
        val factory = renderer.requireClassArgument("rendererFactory", 0, module)

        val component = declaration.findAnnotation(retainedComponentFqName, module) ?: return null
        val data = component.toScreenData(declaration, module)
        //TODO check that navigationHandler type fits to fragment

        val file = FileGenerator().generate(RendererFragmentData(data, factory))
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun generateComposeFragmentCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val composeFragment = declaration.findAnnotation(composeFragmentFqName, module) ?: return null

        val component = declaration.findAnnotation(retainedComponentFqName, module) ?: return null
        val data = component.toScreenData(declaration, module)
        //TODO check that navigationHandler type fits to fragment

        val file = FileGenerator().generate(ComposeFragmentData(data))
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun generateComposeScreenCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        declaration.findAnnotation(composeFqName, module) ?: return null
        val component = declaration.findAnnotation(retainedComponentFqName, module) ?: return null
        val data = component.toScreenData(declaration, module)
        //TODO check that navigationHandler type fits to compose

        val file = FileGenerator().generate(ComposeScreenData(data))
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
    ): SimpleData {
        return SimpleData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = requireClassArgument("scope", 0, module),
            parentScope = requireClassArgument("parentScope", 1, module),
            dependencies = requireClassArgument("dependencies", 2, module),
            stateMachine = requireClassArgument("stateMachine", 3, module),
            navigation = toNavigation(module),
            coroutinesEnabled = optionalBooleanArgument("coroutinesEnabled", 6) ?: false,
            rxJavaEnabled = optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
        )
    }

    private fun KtAnnotationEntry.toNavigation(
        module: ModuleDescriptor
    ): CommonData.Navigation? {
        val navigator = optionalClassArgument("navigator", 4, module)
        val navigationHandler = optionalClassArgument("navigationHandler", 5, module)

        if (navigator != null && navigationHandler != null &&
            navigator != emptyNavigator && navigationHandler != emptyNavigationHandler
        ) {
            return CommonData.Navigation(navigator, navigationHandler)
        }
        if (navigator == null && navigationHandler == null) {
            return null
        }
        if (navigator == emptyNavigator && navigationHandler == emptyNavigationHandler) {
            return null
        }

        throw IllegalStateException("navigator and navigationHandler need to be set together")
    }

    private fun generateNavEntryCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val component = declaration.findAnnotation(navEntryComponentFqName, module) ?: return null
        val data = component.toNavEntryData(declaration, module)
        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun KtAnnotationEntry.toNavEntryData(
        declaration: KtDeclaration,
        module: ModuleDescriptor
    ): NavEntryData {
        val scope = requireClassArgument("scope", 0, module)
        return NavEntryData(
            baseName = scope.simpleName,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = scope,
            parentScope = requireClassArgument("parentScope", 1, module),
            coroutinesEnabled = optionalBooleanArgument("coroutinesEnabled", 2) ?: false,
            rxJavaEnabled = optionalBooleanArgument("rxJavaEnabled", 3) ?: false,
        )
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
