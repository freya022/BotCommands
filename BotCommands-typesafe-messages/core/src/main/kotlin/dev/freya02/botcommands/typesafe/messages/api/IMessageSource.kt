package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Marker interface for "message sources",
 * a message source is an interface with a set of methods which return localized strings,
 * each of them is annotated with [@LocalizedContent][LocalizedContent].
 *
 * No implementation required for interfaces extending this, they will be generated at runtime by your [IMessageSourceFactory].
 *
 * @see IMessageSourceFactory
 * @see IMessageSourceFactory.create
 */
@ExperimentalTypesafeMessagesApi
interface IMessageSource
