package io.github.freya022.botcommands.api.core.service.annotations

/**
 * Defines a message to display when a service of that type has no provider.
 *
 * @see BService @BService
 * @see InterfacedService @InterfacedService
 * @see InjectedService @InjectedService
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class MissingServiceMessage(@get:JvmName("value") val message: String)