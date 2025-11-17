package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

/**
 * A factory of [IMessageSource], interfaces extending this should be annotated with [@MessageSourceFactory][MessageSourceFactory].
 *
 * No implementation required for interfaces extending this, they will be generated at runtime.
 *
 * @param T Type of the generated message sources
 */
@ExperimentalTypesafeMessagesApi
interface IMessageSourceFactory<out T : IMessageSource> {

    /**
     * The name of the bundle this factory will generate message sources for
     */
    val bundleName: String

    /**
     * The effective locales available for this bundle.
     */
    val locales: Set<Locale>

    /**
     * Creates a new message source using the provided [interaction].
     *
     * The default locale will be retrieved from [UserLocaleProvider],
     * while the guild locale will be retrieved from [GuildLocaleProvider].
     */
    fun create(interaction: Interaction): T
}
