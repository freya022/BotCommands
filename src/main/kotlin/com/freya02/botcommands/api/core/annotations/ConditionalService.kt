package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import java.lang.annotation.Inherited

/**
 * Indicates the class is a service that might be available under certain conditions.
 *
 * You will need to implement [ConditionalServiceChecker].
 *
 * @see BService
 * @see InjectedService
 * @see LazyService
 * @see ConditionalServiceChecker
 * @see ServiceType
 */
@Inherited
@Target(AnnotationTarget.CLASS)
annotation class ConditionalService(val message: String = "Conditional object") //Error message may be useful here in case the requested object is an interface
