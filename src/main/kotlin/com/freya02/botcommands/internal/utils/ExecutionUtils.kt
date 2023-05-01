package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.core.options.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.throwInternal
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

private class VarargValue {
    private val values: MutableList<Any?> = arrayListOf()

    @Suppress("UNCHECKED_CAST")
    fun toArray(param: KParameter): Any {
        return when (param.type.jvmErasure) {
            ByteArray::class -> (values.filterNotNull() as List<Byte>).toByteArray()
            ShortArray::class -> (values.filterNotNull() as List<Short>).toShortArray()
            IntArray::class -> (values.filterNotNull() as List<Int>).toIntArray()
            LongArray::class -> (values.filterNotNull() as List<Long>).toLongArray()
            FloatArray::class -> (values.filterNotNull() as List<Float>).toFloatArray()
            DoubleArray::class -> (values.filterNotNull() as List<Double>).toDoubleArray()
            else -> throwInternal("Unsupported primitive array type: ${param.type.jvmErasure}")
        }
    }

    fun add(obj: Any?) {
        values.add(obj)
    }
}

operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameter, obj: Any?): Any? = obj.also {
    this[parameter.kParameter] = obj
}

fun Map<KParameter, Any?>.expandVararg() = mapValues { (param, it) ->
    when (it) {
        is VarargValue -> it.toArray(param)
        else -> it
    }
}

operator fun MutableMap<KParameter, Any?>.set(parameter: AbstractOption, obj: Any?): Any? = obj.also {
    if (parameter.isVararg) {
        (this.getOrPut(parameter.executableParameter) {
            VarargValue()
        } as VarargValue).add(obj)
    } else {
        this[parameter.executableParameter] = obj
    }
}