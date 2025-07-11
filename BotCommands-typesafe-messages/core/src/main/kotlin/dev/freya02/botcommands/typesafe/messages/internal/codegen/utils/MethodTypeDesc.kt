package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import io.github.freya022.botcommands.api.core.utils.mapToArray
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

internal fun KFunction<*>.toMethodTypeDesc(): MethodTypeDesc {
    return MethodTypeDesc.of(
        returnType.jvmErasure.toClassDesc(),
        *valueParameters.mapToArray { it.type.jvmErasure.toClassDesc() }
    )
}
