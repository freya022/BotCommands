package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.parameters.ParameterResolver

/**
 * Marks this class as a [parameter resolver][ParameterResolver].<br>
 * This is a specialization of [BService] for parameter resolvers.
 *
 * **Requirement:** this must extend [ParameterResolver].
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * @see BService @BService
 *
 * @see ParameterResolver
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.BINARY) //Read by ClassGraph
annotation class Resolver