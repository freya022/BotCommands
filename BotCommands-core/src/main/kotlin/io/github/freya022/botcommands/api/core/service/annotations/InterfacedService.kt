package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.GlobalExceptionHandler
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader

/**
 * Marker annotation on interfaces intended to be implemented by a service.
 *
 * If you implement such an interface, your implementation class will need to use [@BService][BService].
 *
 * Implementors of this interface will automatically be registered with the interface's type,
 * in addition to their own type and the ones in [@ServiceType][ServiceType].
 *
 * Retrieval of interfaced services can be done with [ServiceContainer.getInterfacedServices]
 * or [ServiceContainer.getInterfacedServiceTypes].
 * The returned collection is sorted by [service priority][ServicePriority].
 *
 * @see IgnoreServiceTypes @IgnoreServiceTypes
 *
 * @see JDAService
 *
 * @see BotCommandsMessagesFactory
 *
 * @see GlobalExceptionHandler
 *
 * @see LocalizationMapProvider
 * @see LocalizationMapReader
 * @see FormattableArgumentFactory
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InterfacedService(
    /**
     * Determines if multiple implementations of this interfaced service can exist.
     */
    val acceptMultiple: Boolean
)
