package com.freya02.botcommands.api.core.annotations

import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.core.*

/**
 * Marker annotation on interfaces intended to be implemented by a service.
 *
 * If you implement such an interface, your implementation class will need to use [BService],
 * and with the [ServiceType] being the type of the interface being implemented.
 *
 * @see DefaultMessagesSupplier
 * @see SettingsProvider
 * @see GlobalExceptionHandler
 * @see DefaultEmbedSupplier
 * @see DefaultEmbedFooterIconSupplier
 * @see HelpBuilderConsumer
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class InterfacedService
