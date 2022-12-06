package com.freeletics.mad.whetstone.codegen.util

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.FunctionReference
import com.squareup.anvil.compiler.internal.reference.toAnnotationReference
import com.squareup.anvil.compiler.internal.requireFqName
import kotlin.LazyThreadSafetyMode.NONE
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

/**
 * Simplified of [FunctionReference] from Anvil to support top level functions.
 *
 * Used to create a common type between [KtNamedFunction] class references and
 * [FunctionDescriptor] references, to streamline parsing.
 */
@ExperimentalAnvilApi
internal sealed class TopLevelFunctionReference : AnnotatedReference {

  abstract val fqName: FqName
  val name: String get() = fqName.shortName().asString()

  abstract val module: ModuleDescriptor

  abstract val parameters: List<KtParameter>

  override fun toString(): String = "$fqName()"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ClassReference) return false

    if (fqName != other.fqName) return false

    return true
  }

  override fun hashCode(): Int {
    return fqName.hashCode()
  }

  class Psi internal constructor(
    val function: KtFunction,
    override val module: ModuleDescriptor,
    override val fqName: FqName,
  ) : TopLevelFunctionReference() {

    override val annotations: List<AnnotationReference.Psi> by lazy(NONE) {
      function.annotationEntries.map {
        it.toAnnotationReference(declaringClass = null, module)
      }
    }

    override val parameters: List<KtParameter> by lazy(NONE) {
      val kotlinList = mutableListOf<KtParameter>()
      function.getValueParameters().forEach { kotlinList.add(it) }
      kotlinList
    }
  }

  class Descriptor internal constructor(
    val function: FunctionDescriptor,
    override val module: ModuleDescriptor,
    override val fqName: FqName = function.fqNameSafe,
  ) : TopLevelFunctionReference() {

    override val annotations: List<AnnotationReference.Descriptor> by lazy(NONE) {
      function.annotations.map {
        it.toAnnotationReference(declaringClass = null, module)
      }
    }

    override val parameters: List<KtParameter> by lazy(NONE) {
      emptyList()
    }
  }
}

@ExperimentalAnvilApi
internal fun KtFunction.toFunctionReference(
  module: ModuleDescriptor,
): TopLevelFunctionReference.Psi {
  val fqName = requireFqName()
  return TopLevelFunctionReference.Psi(this, module, fqName)
}

@ExperimentalAnvilApi
internal fun FunctionDescriptor.toFunctionReference(
  module: ModuleDescriptor,
): TopLevelFunctionReference.Descriptor {
  return TopLevelFunctionReference.Descriptor(this, module)
}

@ExperimentalAnvilApi
@Suppress("FunctionName")
internal fun AnvilCompilationExceptionTopLevelFunctionReference(
  functionReference: TopLevelFunctionReference,
  message: String,
  cause: Throwable? = null
): AnvilCompilationException = when (functionReference) {
  is TopLevelFunctionReference.Psi -> AnvilCompilationException(
    element = functionReference.function,
    message = message,
    cause = cause
  )
  is TopLevelFunctionReference.Descriptor -> AnvilCompilationException(
    functionDescriptor = functionReference.function,
    message = message,
    cause = cause
  )
}
