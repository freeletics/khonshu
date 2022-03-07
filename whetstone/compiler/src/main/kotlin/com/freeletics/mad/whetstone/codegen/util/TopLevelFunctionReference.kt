package com.freeletics.mad.whetstone.codegen.util

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.AnnotationReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.FunctionReference
import com.squareup.anvil.compiler.internal.reference.toAnnotationReference
import com.squareup.anvil.compiler.internal.requireFqName
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.LazyThreadSafetyMode.NONE
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

/**
 * Simplified of [FunctionReference] from Anvil to support top level functions.
 *
 * Used to create a common type between [KtNamedFunction] class references and
 * [FunctionDescriptor] references, to streamline parsing.
 */
@ExperimentalAnvilApi
public sealed class TopLevelFunctionReference : AnnotatedReference {

  public abstract val fqName: FqName
  public val name: String get() = fqName.shortName().asString()

  public abstract val module: ModuleDescriptor

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

  public class Psi internal constructor(
    public val function: KtFunction,
    public override val module: ModuleDescriptor,
    public override val fqName: FqName,
  ) : TopLevelFunctionReference() {

    public override val annotations: List<AnnotationReference.Psi> by lazy(NONE) {
      function.annotationEntries.map {
        it.toAnnotationReference(declaringClass = null, module)
      }
    }
  }

  public class Descriptor internal constructor(
    public val function: FunctionDescriptor,
    public override val module: ModuleDescriptor,
    public override val fqName: FqName = function.fqNameSafe,
  ) : TopLevelFunctionReference() {

    public override val annotations: List<AnnotationReference.Descriptor> by lazy(NONE) {
      function.annotations.map {
        it.toAnnotationReference(declaringClass = null, module)
      }
    }
  }
}

@ExperimentalAnvilApi
public fun KtFunction.toFunctionReference(
  module: ModuleDescriptor,
): TopLevelFunctionReference.Psi {
  val fqName = requireFqName()
  return TopLevelFunctionReference.Psi(this, module, fqName)
}

@ExperimentalAnvilApi
public fun FunctionDescriptor.toFunctionReference(
  module: ModuleDescriptor,
): TopLevelFunctionReference.Descriptor {
  return TopLevelFunctionReference.Descriptor(this, module)
}

@ExperimentalAnvilApi
@Suppress("FunctionName")
public fun AnvilCompilationExceptionTopLevelFunctionReference(
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
