package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Marker interface for "message sources",
 * which **must** be an interface with a set of methods returning localized strings,
 * each of them is annotated with [@LocalizedContent][LocalizedContent].
 *
 * An exception will be thrown if any additional *abstract* (i.e. without a default) method is found.
 *
 * No implementation is required for interfaces extending this, they will be generated at runtime by your [IMessageSourceFactory].
 *
 * @see IMessageSourceFactory
 * @see IMessageSourceFactory.create
 */
@ExperimentalTypesafeMessagesApi
interface IMessageSource
