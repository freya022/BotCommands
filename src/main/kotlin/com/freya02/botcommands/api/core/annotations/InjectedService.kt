package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.core.config.BComponentsConfig

/**
 * Annotates a class as an injected service.
 *
 * The service needs to be instantiated and registered manually.
 *
 * This may be good for situations where services are defined by strategies (see Strategy design pattern),
 * an example would be [BComponentsConfig.componentManagerStrategy].
 *
 * @see BService
 * @see ConditionalService
 * @see ServiceType
 */
@Target(AnnotationTarget.CLASS)
annotation class InjectedService(val message: String = "This service does not exist yet, it may be created under certain conditions")
