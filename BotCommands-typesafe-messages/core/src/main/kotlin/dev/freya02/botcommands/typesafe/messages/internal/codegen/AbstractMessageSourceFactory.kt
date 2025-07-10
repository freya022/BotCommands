package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass

internal abstract class AbstractMessageSourceFactory internal constructor(
    private val params: Params,
) : IMessageSourceFactory<IMessageSource> {

    override fun create(interaction: Interaction): IMessageSource {
        val (localizationService, bundle, guildLocaleProvider, userLocaleProvider, sourceType) = params

        val localizationContext = LocalizationContext.create(
            localizationService = localizationService,
            localizationBundle = bundle,
            localizationPrefix = null,
            guildLocale = guildLocaleProvider.getDiscordLocale(interaction),
            userLocale = userLocaleProvider.getDiscordLocale(interaction),
        )

        return MessageSourceGenerator.create(sourceType, localizationContext)
    }

    internal data class Params(
        internal val localizationService: LocalizationService,
        internal val bundle: String,
        internal val guildLocaleProvider: GuildLocaleProvider,
        internal val userLocaleProvider: UserLocaleProvider,
        internal val sourceType: KClass<out IMessageSource>,
    )
}
