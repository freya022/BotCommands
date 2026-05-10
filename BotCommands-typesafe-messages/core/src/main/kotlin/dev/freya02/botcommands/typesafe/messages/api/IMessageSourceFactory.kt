package dev.freya02.botcommands.typesafe.messages.api

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.Interaction
import java.util.Locale

/**
 * A factory of [IMessageSource], this **must** be extended by an interface annotated with [@MessageSourceFactory][MessageSourceFactory].
 *
 * An exception will be thrown if any additional *abstract* (i.e. without a default) method is found.
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
     * The default/user locale will be retrieved from [UserLocaleProvider],
     * while the guild locale will be retrieved from [GuildLocaleProvider].
     */
    fun create(interaction: Interaction): T

    /**
     * Creates a new message source using the provided message.
     *
     * The default locale will be retrieved from [MessageLocaleProvider].
     *
     * @throws IllegalStateException If the text commands module is absent
     */
    fun create(message: Message): T

    /**
     * Creates a new message source using the provided guild locale, and no user locale.
     */
    fun create(guildLocale: Locale): T = create(guildLocale, userLocale = null)

    /**
     * Creates a new message source using the provided guild and user locale.
     */
    fun create(guildLocale: Locale, userLocale: Locale?): T
}
