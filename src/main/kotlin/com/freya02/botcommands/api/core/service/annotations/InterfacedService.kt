package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.core.*
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.DynamicSupplier

/**
 * Marker annotation on interfaces intended to be implemented by a service.
 *
 * If you implement such an interface, your implementation class will need to use [BService],
 * and with the [ServiceType] being the type of the interface being implemented.
 *
 * @see DynamicSupplier
 * @see DefaultMessagesSupplier
 * @see SettingsProvider
 * @see GlobalExceptionHandler
 * @see DefaultEmbedSupplier
 * @see DefaultEmbedFooterIconSupplier
 * @see IHelpCommand
 * @see HelpBuilderConsumer
 * @see AutocompleteTransformer
 * @see ConnectionSupplier
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
