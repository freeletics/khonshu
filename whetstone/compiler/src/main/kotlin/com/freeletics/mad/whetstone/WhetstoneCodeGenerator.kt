package com.freeletics.mad.whetstone

import com.freeletics.mad.whetstone.codegen.FileGenerator
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.composeNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.composeRootNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.freeletics.mad.whetstone.codegen.util.moduleFqName
import com.freeletics.mad.whetstone.codegen.util.fragmentNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.fragmentNavEventNavigator
import com.freeletics.mad.whetstone.codegen.util.fragmentRootNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.navEntryComponentFqName
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
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
public class WhetstoneCodeGenerator : CodeGenerator {

    override fun isApplicable(context: AnvilContext): Boolean = !context.disableComponentMerging

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): Collection<GeneratedFile> {
        val rendererFragment = projectFiles
            .classesAndInnerClass(module)
            .mapNotNull { clazz -> generateRendererCode(codeGenDir, module, clazz) }

        val composeScreen = projectFiles
            .flatMap { it.declarations.filterIsInstance<KtNamedFunction>() }
            .mapNotNull { function -> generateComposeScreenCode(codeGenDir, module, function) }

        val composeFragment = projectFiles
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
        val data = RendererFragmentData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageName(),
            scope = renderer.requireClassArgument("scope", 0, module),
            parentScope = renderer.requireClassArgument("parentScope", 1, module),
            dependencies = renderer.requireClassArgument("dependencies", 2, module),
            stateMachine = renderer.requireClassArgument("stateMachine", 3, module),
            factory = renderer.requireClassArgument("rendererFactory", 4, module),
            fragmentBaseClass = renderer.optionalClassArgument("fragmentBaseClass", 5, module) ?: fragment,
            coroutinesEnabled = renderer.optionalBooleanArgument("coroutinesEnabled", 6) ?: false,
            rxJavaEnabled = renderer.optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
            navigation = fragmentNavigation(module, declaration),
        )

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
        val compose = declaration.findAnnotation(composeFragmentFqName, module) ?: return null
        val data = ComposeFragmentData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageName(),
            scope = compose.requireClassArgument("scope", 0, module),
            parentScope = compose.requireClassArgument("parentScope", 1, module),
            dependencies = compose.requireClassArgument("dependencies", 2, module),
            stateMachine = compose.requireClassArgument("stateMachine", 3, module),
            fragmentBaseClass = compose.optionalClassArgument("fragmentBaseClass", 4, module) ?: fragment,
            enableInsetHandling = compose.optionalBooleanArgument("enableInsetHandling", 5) ?: false,
            coroutinesEnabled = compose.optionalBooleanArgument("coroutinesEnabled", 6) ?: false,
            rxJavaEnabled = compose.optionalBooleanArgument("rxJavaEnabled", 7) ?: false,
            navigation = fragmentNavigation(module, declaration),
        )

        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun fragmentNavigation(
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): CommonData.Navigation? {
        val navigation = declaration.findAnnotation(fragmentNavDestinationFqName, module)
        if (navigation != null) {
            return CommonData.Navigation(
                navigator = fragmentNavEventNavigator,
                route = navigation.requireClassArgument("route", 0, module),
            )
        }
        val rootNavigation = declaration.findAnnotation(fragmentRootNavDestinationFqName, module)
        if (rootNavigation != null) {
            return CommonData.Navigation(
                navigator = fragmentNavEventNavigator,
                route = rootNavigation.requireClassArgument("root", 0, module),
            )
        }
        return null
    }

    private fun generateComposeScreenCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): GeneratedFile? {
        val compose = declaration.findAnnotation(composeFqName, module) ?: return null
        val data = ComposeScreenData(
            baseName = declaration.name!!,
            packageName = declaration.containingKtFile.packageName(),
            scope = compose.requireClassArgument("scope", 0, module),
            parentScope = compose.requireClassArgument("parentScope", 1, module),
            dependencies = compose.requireClassArgument("dependencies", 2, module),
            stateMachine = compose.requireClassArgument("stateMachine", 3, module),
            coroutinesEnabled = compose.optionalBooleanArgument("coroutinesEnabled", 4) ?: false,
            rxJavaEnabled = compose.optionalBooleanArgument("rxJavaEnabled", 5) ?: false,
            navigation = composeNavigation(module, declaration),
        )

        val file = FileGenerator().generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = file.packageName,
            fileName = file.name,
            content = file.toString()
        )
    }

    private fun composeNavigation(
        module: ModuleDescriptor,
        declaration: KtDeclaration
    ): CommonData.Navigation? {
        val navigation = declaration.findAnnotation(composeNavDestinationFqName, module)
        if (navigation != null) {
            return CommonData.Navigation(
                navigator = navEventNavigator,
                route = navigation.requireClassArgument("route", 0, module),
            )
        }
        val rootNavigation = declaration.findAnnotation(composeRootNavDestinationFqName, module)
        if (rootNavigation != null) {
            return CommonData.Navigation(
                navEventNavigator,
                route = rootNavigation.requireClassArgument("root", 0, module),
            )
        }
        return null
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
            packageName = declaration.containingKtFile.packageName(),
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

    private fun KtAnnotationEntry.optionalClassArgumentIfNot(
        name: String,
        index: Int,
        module: ModuleDescriptor,
        filteredOutClass: ClassName,
    ): ClassName? {
        val className = optionalClassArgument(name, index, module)
        if (className != filteredOutClass) {
            return className
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

    private fun KtFile.packageName(): String {
        return packageFqName.pathSegments().joinToString(separator = ".")
    }
}
