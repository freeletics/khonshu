package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.FileGenerator
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.dialogFragment
import com.freeletics.mad.whetstone.codegen.util.emptyNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.emptyNavigator
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.freeletics.mad.whetstone.codegen.util.moduleFqName
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentFqName
import com.freeletics.mad.whetstone.codegen.util.rendererDialogFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.rendererFragmentFqName
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
        val rendererDialogFragment = projectFiles
            .classesAndInnerClass(module)
            .mapNotNull { clazz -> generateRendererDialogCode(codeGenDir, module, clazz) }

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

        return rendererFragment.toList() + rendererDialogFragment +
                composeFragment + composeScreen + navEntry
    }

    private fun generateRendererCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val renderer = declaration.findAnnotation(rendererFragmentFqName, module) ?: return null
        val data = RendererFragmentData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = renderer.requireClassArgument("scope", 0, module),
            parentScope = renderer.requireClassArgument("parentScope", 1, module),
            dependencies = renderer.requireClassArgument("dependencies", 2, module),
            factory = renderer.requireClassArgument("rendererFactory", 3, module),
            stateMachine = renderer.requireClassArgument("stateMachine", 4, module),
            fragmentBaseClass = fragment,
            navigation = renderer.toNavigation(5, 6, module),
            coroutinesEnabled = renderer.optionalBooleanArgument("coroutinesEnabled", 7) ?: false,
            rxJavaEnabled = renderer.optionalBooleanArgument("rxJavaEnabled", 8) ?: false,
        )
        //TODO check that navigationHandler type fits to fragment

        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun generateRendererDialogCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val renderer = declaration.findAnnotation(rendererDialogFragmentFqName, module) ?: return null
        val data = RendererFragmentData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = renderer.requireClassArgument("scope", 0, module),
            parentScope = renderer.requireClassArgument("parentScope", 1, module),
            dependencies = renderer.requireClassArgument("dependencies", 2, module),
            factory = renderer.requireClassArgument("rendererFactory", 3, module),
            stateMachine = renderer.requireClassArgument("stateMachine", 4, module),
            fragmentBaseClass = renderer.optionalClassArgument("dialogFragmentBaseClass", 5, module) ?: dialogFragment,
            navigation = renderer.toNavigation(6, 7, module),
            coroutinesEnabled = renderer.optionalBooleanArgument("coroutinesEnabled", 8) ?: false,
            rxJavaEnabled = renderer.optionalBooleanArgument("rxJavaEnabled", 9) ?: false,
        )
        //TODO check that navigationHandler type fits to fragment

        val file = FileGenerator().generate(data)
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
        val data = ComposeFragmentData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = composeFragment.requireClassArgument("scope", 0, module),
            parentScope = composeFragment.requireClassArgument("parentScope", 1, module),
            dependencies = composeFragment.requireClassArgument("dependencies", 2, module),
            stateMachine = composeFragment.requireClassArgument("stateMachine", 3, module),
            enableInsetHandling = composeFragment.optionalBooleanArgument("enableInsetHandling", 4) ?: false,
            navigation = composeFragment.toNavigation(5, 6, module),
            coroutinesEnabled = composeFragment.optionalBooleanArgument("coroutinesEnabled", 7) ?: false,
            rxJavaEnabled = composeFragment.optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
        )
        //TODO check that navigationHandler type fits to fragment

        val file = FileGenerator().generate(data)
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
        val compose = declaration.findAnnotation(composeFqName, module) ?: return null
        val data = ComposeScreenData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = compose.requireClassArgument("scope", 0, module),
            parentScope = compose.requireClassArgument("parentScope", 1, module),
            dependencies = compose.requireClassArgument("dependencies", 2, module),
            stateMachine = compose.requireClassArgument("stateMachine", 3, module),
            navigation = compose.toNavigation(4, 5, module),
            coroutinesEnabled = compose.optionalBooleanArgument("coroutinesEnabled", 6) ?: false,
            rxJavaEnabled = compose.optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
        )
        //TODO check that navigationHandler type fits to compose

        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun KtAnnotationEntry.toNavigation(
        navigatorIndex: Int,
        navigationHandlerIndex: Int,
        module: ModuleDescriptor
    ): CommonData.Navigation? {
        val navigator = optionalClassArgument("navigator", navigatorIndex, module)
        val navigationHandler = optionalClassArgument("navigationHandler", navigationHandlerIndex, module)

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
        val scope = component.requireClassArgument("scope", 0, module)
        val data = NavEntryData(
            baseName = scope.simpleName,
            packageName = declaration.containingKtFile.packageFqName.pathSegments().joinToString(separator = "."),
            scope = scope,
            parentScope = component.requireClassArgument("parentScope", 1, module),
            coroutinesEnabled = component.optionalBooleanArgument("coroutinesEnabled", 2) ?: false,
            rxJavaEnabled = component.optionalBooleanArgument("rxJavaEnabled", 3) ?: false,
        )

        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
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
