# Allow to make some classes public so that we can repackage them without breaking package-private members
-allowaccessmodification

# Keep kotlin metadata so that the Kotlin compiler knows about top level functions and other things
-keep class kotlin.Metadata { *; }

# Keep FunctionX because they are used in the public API of Gradle/AGP/KGP
-keep class kotlin.jvm.functions.** { *; }

# Keep Unit for kts compatibility, functions in a Gradle extension returning a relocated Unit won't work
-keep class kotlin.Unit

# We need to keep type arguments (Signature) for Gradle to be able to instantiate abstract models like `Property`
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses,PermittedSubclasses,EnclosingMethod,Deprecated,SourceFile,LineNumberTable

# Keep your public API so that it's callable from scripts
-keep class com.freeletics.khonshu.deeplinks.plugin.** { public *; }
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers @com.squareup.moshi.JsonClass @kotlin.Metadata class * {
    synthetic <init>(...);
}

# No need to obfuscate class names
-dontobfuscate

-repackageclasses com.freeletics.khonshu.deeplinks.plugin.relocated
