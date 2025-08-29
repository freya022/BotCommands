package dev.freya02.botcommands.method.accessors.internal.codegen.utils

import java.lang.classfile.CodeBuilder
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc

internal fun CodeBuilder.unboxOrCastTo(target: Class<*>) {
    when (target) {
        Boolean::class.javaPrimitiveType -> {
            checkcast(CD_Boolean)
            invokevirtual(CD_Boolean, "booleanValue", MethodTypeDesc.of(CD_boolean))
        }
        Byte::class.javaPrimitiveType -> {
            checkcast(CD_Byte)
            invokevirtual(CD_Byte, "byteValue", MethodTypeDesc.of(CD_byte))
        }
        Char::class.javaPrimitiveType -> {
            checkcast(CD_Character)
            invokevirtual(CD_Character, "charValue", MethodTypeDesc.of(CD_char))
        }
        Short::class.javaPrimitiveType -> {
            checkcast(CD_Short)
            invokevirtual(CD_Short, "shortValue", MethodTypeDesc.of(CD_short))
        }
        Int::class.javaPrimitiveType -> {
            checkcast(CD_Integer)
            invokevirtual(CD_Integer, "intValue", MethodTypeDesc.of(CD_int))
        }
        Long::class.javaPrimitiveType -> {
            checkcast(CD_Long)
            invokevirtual(CD_Long, "longValue", MethodTypeDesc.of(CD_long))
        }
        Float::class.javaPrimitiveType -> {
            checkcast(CD_Float)
            invokevirtual(CD_Float, "floatValue", MethodTypeDesc.of(CD_float))
        }
        Double::class.javaPrimitiveType -> {
            checkcast(CD_Double)
            invokevirtual(CD_Double, "doubleValue", MethodTypeDesc.of(CD_double))
        }
        else -> {
            checkcast(target.describeConstable().get())
        }
    }
}
