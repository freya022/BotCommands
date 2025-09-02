package dev.freya02.botcommands.method.accessors.internal.codegen.utils

import java.lang.classfile.CodeBuilder
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KClass

/**
 * Consumes the top stack value
 */
internal fun CodeBuilder.ifNull(onNull: () -> Unit, onNonNull: () -> Unit) {
    val ifNullLabel = newLabel()
    val resumeLabel = newLabel()

    // If stack top value is null then jump
    ifnull(ifNullLabel)
    // At this point the value is non-null
    onNonNull()
    goto_(resumeLabel) // Skip null case

    labelBinding(ifNullLabel)
    // At this point the value is null
    onNull()

    labelBinding(resumeLabel)
}

/**
 * NOTE: [target] != [kotlinErasure].java due to value classes, do not pass KClass.java
 */
internal fun CodeBuilder.unboxOrCastTo(target: Class<*>, kotlinErasure: KClass<*>) {
    if (kotlinErasure.isValue) {
        val valuePropertyDesc = kotlinErasure.java.declaredFields[0].type.describeConstable().get()
        val valueClassDesc = kotlinErasure.java.describeConstable().get()
        checkcast(valueClassDesc)
        invokevirtual(valueClassDesc, "unbox-impl", MethodTypeDesc.of(valuePropertyDesc))
        return
    }

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

internal fun CodeBuilder.boxIfPrimitive(type: Class<*>) {
    when (type) {
        Boolean::class.javaPrimitiveType -> {
            invokestatic(CD_Boolean, "valueOf", MethodTypeDesc.of(CD_Boolean, CD_boolean))
        }
        Byte::class.javaPrimitiveType -> {
            invokestatic(CD_Byte, "valueOf", MethodTypeDesc.of(CD_Byte, CD_byte))
        }
        Char::class.javaPrimitiveType -> {
            invokestatic(CD_Character, "valueOf", MethodTypeDesc.of(CD_Character, CD_char))
        }
        Short::class.javaPrimitiveType -> {
            invokestatic(CD_Short, "valueOf", MethodTypeDesc.of(CD_Short, CD_short))
        }
        Int::class.javaPrimitiveType -> {
            invokestatic(CD_Integer, "valueOf", MethodTypeDesc.of(CD_Integer, CD_int))
        }
        Long::class.javaPrimitiveType -> {
            invokestatic(CD_Long, "valueOf", MethodTypeDesc.of(CD_Long, CD_long))
        }
        Float::class.javaPrimitiveType -> {
            invokestatic(CD_Float, "valueOf", MethodTypeDesc.of(CD_Float, CD_float))
        }
        Double::class.javaPrimitiveType -> {
            invokestatic(CD_Double, "valueOf", MethodTypeDesc.of(CD_Double, CD_double))
        }
        else -> {}
    }
}
