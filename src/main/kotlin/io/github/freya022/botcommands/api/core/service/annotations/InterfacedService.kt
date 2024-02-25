package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextSuggestionSupplier
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.*
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.db.query.ParametrizedQueryFactory
import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.localization.arguments.factories.FormattableArgumentFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader

/**
 * Marker annotation on interfaces intended to be implemented by a service.
 *
 * If you implement such an interface, your implementation class will need to use [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * Implementors of this interface will automatically be registered with the interface's type,
 * in addition to their own type and the ones in [@ServiceType][ServiceType].
 *
 * Retrieval of interfaced services can be done with [ServiceContainer.getInterfacedServices]
 * or [ServiceContainer.getInterfacedServiceTypes].
 * The returned collection is sorted by [service priority][ServicePriority].
 *
 * @see DynamicSupplier
 *
 * @see JDAService
 *
 * @see DefaultMessagesSupplier
 *
 * @see SettingsProvider
 *
 * @see GlobalExceptionHandler
 *
 * @see TextCommandProvider
 * @see TextCommandFilter
 * @see DefaultEmbedSupplier
 * @see DefaultEmbedFooterIconSupplier
 * @see IHelpCommand
 * @see HelpBuilderConsumer
 * @see TextSuggestionSupplier
 *
 * @see GlobalApplicationCommandProvider
 * @see GuildApplicationCommandProvider
 * @see AutocompleteDeclaration
 * @see AutocompleteTransformer
 * @see ApplicationCommandFilter
 *
 * @see ConnectionSupplier
 * @see ParametrizedQueryFactory
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
