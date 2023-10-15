package io.github.freya022.botcommands.api.parameters

import kotlin.reflect.KClass

abstract class ClassParameterResolver<T : ClassParameterResolver<T, R>, R : Any>(
    val jvmErasure: KClass<R>
) : ParameterResolver<T, R>() {
    constructor(clazz: Class<R>) : this(clazz.kotlin)
}