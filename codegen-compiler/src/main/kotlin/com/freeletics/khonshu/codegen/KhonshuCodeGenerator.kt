package com.freeletics.khonshu.codegen

import com.freeletics.khonshu.codegen.codegen.FileGenerator
import com.freeletics.khonshu.codegen.parser.anvil.toComposeFragmentDestinationData
import com.freeletics.khonshu.codegen.parser.anvil.toComposeScreenDestinationData
import com.freeletics.khonshu.codegen.parser.anvil.toNavHostActivityData
import com.freeletics.khonshu.codegen.parser.anvil.toRendererFragmentDestinationData
import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
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
                listOfNotNull(
                    it.toComposeScreenDestinationData(),
                    it.toComposeFragmentDestinationData(),
                    it.toNavHostActivityData(),
                )
            }

        val renderer = projectFiles
            .classAndInnerClassReferences(module)
            .mapNotNull {
                it.toRendererFragmentDestinationData()
            }

        val data = compose.toList() + renderer

        return data.map {
            val file = FileGenerator().generate(it)
            createGeneratedFile(
                codeGenDir = codeGenDir,
                packageName = file.packageName,
                fileName = file.name,
                content = file.toString(),
            )
        }
    }
}
