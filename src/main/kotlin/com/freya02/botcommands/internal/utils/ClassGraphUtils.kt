package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.core.utils.shortQualifiedName
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import kotlin.reflect.KClass

internal val ClassInfo.simpleNestedName
    get() = name.dropWhile { !it.isUpperCase() }

internal val MethodInfo.shortSignatureNoSrc: String
    get() {
        val declaringClassName = this.classInfo.simpleNestedName
        val methodName = this.name
        //Suboptimal, but we can't map a MethodInfo to a KFunction due to overloads
        val parameters = this.parameterInfo
            .filter { it.typeDescriptor.toString() != "kotlin.coroutines.Continuation" }
            .joinToString { it.typeSignatureOrTypeDescriptor.toStringWithSimpleNames() }
        return "$declaringClassName.$methodName($parameters)"
    }

internal val MethodInfo.shortSignature: String
    get() {
        val returnType = this.typeSignatureOrTypeDescriptor.resultType.toStringWithSimpleNames()
        val sourceFile = classInfo.sourceFile ?: "<no-source>"
        val source = if (minLineNum != 0) "$sourceFile:$minLineNum" else sourceFile
        return "$shortSignatureNoSrc: $returnType ($source)"
    }

fun ClassInfo.toShortSignature(kClass: KClass<*>): String = "${kClass.shortQualifiedName}(${this.sourceFile}:0)"