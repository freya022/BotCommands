package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.service.ServiceContainer

/**
 * No-op annotation marking a class as an injected service,
 * i.e., one that isn't registered by a class annotation or a service factory.
 *
 * For abstract classes/interfaces annotated with this,
 * you can request an instance in any service,
 * if it is available.
 *
 * If you are making such service, it has to be registered manually via [ServiceContainer.putService].
 *
 * @see BService @BService
 * @see MissingServiceMessage @MissingServiceMessage
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class InjectedService
