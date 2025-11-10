package dev.freya02.botcommands.typesafe.messages.api.annotations

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider
import org.springframework.stereotype.Component

/**
 * Mandatory annotation for interfaces extending [IMessageSourceFactory].
 *
 * @see bundleName
 */
@Component
@ExperimentalTypesafeMessagesApi
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class MessageSourceFactory(
    /**
     * The name of the bundle to get the templates from.
     *
     * The bundles will be searched using the accessible [localization map providers][LocalizationMapProvider].
     */
    @get:JvmName("value")
    val bundleName: String
)
