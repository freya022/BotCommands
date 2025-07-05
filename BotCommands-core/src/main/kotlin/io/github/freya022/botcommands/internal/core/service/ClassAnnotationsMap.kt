package io.github.freya022.botcommands.internal.core.service

import kotlin.reflect.KClass

// No need for @InterfacedService
interface ClassAnnotationsMap {
    fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>?
}

internal inline fun <reified A : Annotation> ClassAnnotationsMap.getOrNull(): Set<KClass<*>>? = getOrNull(A::class)

internal inline fun <reified A : Annotation> ClassAnnotationsMap.get(): Set<KClass<*>> = getOrNull<A>() ?: emptySet()
