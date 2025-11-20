package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import dev.freya02.botcommands.typesafe.messages.internal.annotations.DynamicCall
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.lang.invoke.MethodHandle
import java.util.*

internal abstract class AbstractMessageSourceFactory<out T : IMessageSource> @DynamicCall internal constructor(
    private val params: Params,
) : IMessageSourceFactory<T> {

    override val bundleName: String get() = params.bundle
    override val locales: Set<Locale> = params.locales

    override fun create(interaction: Interaction): T {
        val (localizationService, bundle, _, guildLocaleProvider, userLocaleProvider, sourceHandle) = params

        val messageSourceContext = MessageSourceContext(
            localizationService = localizationService,
            localizationBundle = bundle,
            guildLocale = guildLocaleProvider.getDiscordLocale(interaction),
            userLocale = userLocaleProvider.getDiscordLocale(interaction),
        )

        return MessageSourceGenerator.instantiate(sourceHandle, messageSourceContext)
    }

    internal data class Params(
        internal val localizationService: LocalizationService,
        internal val bundle: String,
        internal val locales: Set<Locale>,
        internal val guildLocaleProvider: GuildLocaleProvider,
        internal val userLocaleProvider: UserLocaleProvider,
        internal val sourceHandle: MethodHandle,
    )
}
