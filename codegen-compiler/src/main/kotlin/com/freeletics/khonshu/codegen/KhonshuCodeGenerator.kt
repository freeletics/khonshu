package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.FileGenerator
import com.freeletics.khonshu.codegen.parser.toBaseData
import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFileWithSources
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.containingFileAsJavaFile
import com.squareup.anvil.compiler.internal.reference.topLevelFunctionReferences
import java.io.File
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile

@AutoService(CodeGenerator::class)
public class KhonshuCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFileWithSources> {
        val generator = FileGenerator()
        return projectFiles
            .topLevelFunctionReferences(module)
            .flatMap {
                val sourceFile by lazy(LazyThreadSafetyMode.NONE) {
                    it.function.containingFileAsJavaFile()
                }
                it.toBaseData().map { generator.generate(it, codeGenDir, sourceFile) }
            }
            .toList()
    }

    private fun FileGenerator.generate(data: BaseData, codeGenDir: File, sourceFile: File): GeneratedFileWithSources {
        val fileSpec = generate(data)
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = fileSpec.packageName,
            fileName = fileSpec.name,
            content = fileSpec.toString(),
            sourceFile = sourceFile,
        )
    }
}
