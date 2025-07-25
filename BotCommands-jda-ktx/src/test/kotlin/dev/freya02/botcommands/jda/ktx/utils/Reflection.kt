package dev.freya02.botcommands.jda.ktx.utils

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Any> KType.findErasureOfAt(index: Int): KType = findErasureOfAt(index, T::class)

fun KType.findErasureOfAt(index: Int, targetType: KClass<*>): KType {
    if (this.jvmErasure == targetType) {
        return this.arguments[index].type
            ?: error("Star projections are not allowed on argument #$index of ${targetType.qualifiedName}")
    }

    return this.jvmErasure.superErasureAt(index, targetType)
}

fun KClass<*>.superErasureAt(index: Int, targetType: KClass<*>): KType {
    val interfaceType = allSupertypes.firstOrNull { it.jvmErasure == targetType }
        ?: error("Unable to find the supertype '${targetType.qualifiedName}' in '${this.qualifiedName}'")
    return interfaceType.arguments[index].type
        ?: error("Star projections are not allowed on argument #$index of ${targetType.qualifiedName}")
}
