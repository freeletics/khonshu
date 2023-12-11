package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.FileGenerator
import com.freeletics.khonshu.codegen.parser.toComposeScreenDestinationData
import com.freeletics.khonshu.codegen.parser.toNavHostActivityData
import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
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
    ): Collection<GeneratedFile> {
        val compose = projectFiles
            .topLevelFunctionReferences(module)
            .flatMap {
                listOfNotNull(it.toComposeScreenDestinationData()) +
                    it.toNavHostActivityData()
            }

        return compose.map {
            val file = FileGenerator().generate(it)
            createGeneratedFile(
                codeGenDir = codeGenDir,
                packageName = file.packageName,
                fileName = file.name,
                content = file.toString(),
            )
        }.toList()
    }
}
