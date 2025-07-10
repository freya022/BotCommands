package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import java.lang.classfile.CodeBuilder
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KClass

internal fun CodeBuilder.ldc(string: String) {
    ldc(string as java.lang.String)
}

/**
 * Boxes the value on the top of the stack if the [type] is a primitive, the boxed value is on the top of the stack.
 */
internal fun CodeBuilder.boxIfNecessary(type: KClass<*>) {
    type.javaPrimitiveType?.let { javaPrimitive ->
        when (javaPrimitive) {
            Boolean::class.java -> boxTo<Boolean>()
            Byte::class.java -> boxTo<Byte>()
            Char::class.java -> boxTo<Char>()
            Short::class.java -> boxTo<Short>()
            Int::class.java -> boxTo<Int>()
            Long::class.java -> boxTo<Long>()
            Float::class.java -> boxTo<Float>()
            Double::class.java -> boxTo<Double>()
        }
    }
}

private inline fun <reified T : Any> CodeBuilder.boxTo() {
    val primitiveType = T::class.javaPrimitiveType!!.toClassDesc()
    val objectType = T::class.javaObjectType.toClassDesc()
    invokestatic(objectType, "valueOf", MethodTypeDesc.of(objectType, primitiveType))
}
