package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.core.config.BComponentsConfig
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Marks a service as requiring other services.
 *
 * Services that miss dependencies will not be instantiated and won't throw an exception.
 *
 * You can, for example, use this annotation on commands which require components to be enabled;
 * this will enable you to disable [BComponentsConfig.useComponents] without disabling commands manually.
 *
 * @see BService @BService
 * @see ConditionalService @ConditionalService
 * @see InjectedService @InjectedService
 */
@Inherited
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Dependencies(
    /**
     * An array of services required by this service.
     */
    vararg val value: KClass<*>
)
