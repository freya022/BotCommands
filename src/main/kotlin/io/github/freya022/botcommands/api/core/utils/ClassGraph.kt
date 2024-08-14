package io.github.freya022.botcommands.api.core.utils

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo

val ClassInfo.simpleNestedName
    get() = name.dropWhile { !it.isUpperCase() }

val ClassInfo.shortQualifiedName
    get() = packageName.split('.').joinToString(".") { it.first().toString() } + "." + simpleNestedName

val MethodInfo.shortSignatureNoSrc: String
    get() {
        val declaringClassName = this.classInfo.simpleNestedName
        val methodName = this.name
        //Suboptimal, but we can't map a MethodInfo to a KFunction due to overloads
        val parameters = this.parameterInfo
            .filter { it.typeDescriptor.toString() != "kotlin.coroutines.Continuation" }
            .joinToString { it.typeSignatureOrTypeDescriptor.toStringWithSimpleNames() }
        return "$declaringClassName.$methodName($parameters)"
    }
val MethodInfo.shortSignature: String
    get() {
        val returnType = this.typeSignatureOrTypeDescriptor.resultType.toStringWithSimpleNames()
        val sourceFile = classInfo.sourceFile ?: "<no-source>"
        val source = if (minLineNum != 0) "$sourceFile:$minLineNum" else sourceFile
        return "$shortSignatureNoSrc: $returnType ($source)"
    }

@Deprecated("Replaced by shortQualifiedName", ReplaceWith("shortQualifiedName"))
val ClassInfo.shortQualifiedReference: String
    inline get() = shortQualifiedName