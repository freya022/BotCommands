package io.github.freya022.botcommands.internal.core.service

import kotlin.reflect.KClass

// No need for @InterfacedService
interface InstantiableServices {
    fun getAllPrimaryTypes(): Set<KClass<*>>
}
