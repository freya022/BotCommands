package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.prefixed.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.prefixed.IHelpCommand
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.*
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader

/**
 * Marker annotation on interfaces intended to be implemented by a service.
 *
 * If you implement such an interface, your implementation class will need to use [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * Implementors of this interface will automatically be registered with the interface's type,
 * in addition to their own type and the ones in [ServiceType]
 *
 * @see DynamicSupplier
 *
 * @see DefaultMessagesSupplier
 *
 * @see SettingsProvider
 *
 * @see GlobalExceptionHandler
 *
 * @see TextCommandFilter
 * @see DefaultEmbedSupplier
 * @see DefaultEmbedFooterIconSupplier
 * @see IHelpCommand
 * @see HelpBuilderConsumer
 *
 * @see AutocompleteTransformer
 * @see ApplicationCommandFilter
 *
 * @see ConnectionSupplier
 *
 * @see ComponentInteractionFilter
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