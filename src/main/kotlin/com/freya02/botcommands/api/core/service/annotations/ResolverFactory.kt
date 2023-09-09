package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.parameters.ParameterResolverFactory

/**
 * Marks this class as a [parameter resolver factory][ParameterResolverFactory].<br>
 * This is a specialization of [BService] for parameter resolver factories.
 *
 * **Requirement:** this must extend [ParameterResolverFactory].
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * @see BService @BService
 *
 * @see ParameterResolverFactory
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.BINARY) //Read by ClassGraph
annotation class ResolverFactory  