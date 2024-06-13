package io.github.freya022.botcommands.internal.parameters.resolvers.localization

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.localization.LocalizationContextImpl
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction

internal class AppLocalizationContextResolver(
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val baseContext: LocalizationContextImpl,
) : ClassParameterResolver<AppLocalizationContextResolver, AppLocalizationContext>(AppLocalizationContext::class),
    ICustomResolver<AppLocalizationContextResolver, AppLocalizationContext> {

    override suspend fun resolveSuspend(executable: Executable, event: Event): AppLocalizationContext {
        return when (event) {
            is Interaction -> baseContext.withLocales(guildLocaleProvider.getDiscordLocale(event), userLocaleProvider.getDiscordLocale(event))
            //MessageReceivedEvent does not provide user locale
            else -> throwInternal("Unsupported event type for ${classRef<AppLocalizationContext>()}: ${event.javaClass.name}")
        }
    }
}

internal class TextLocalizationContextResolver(
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val textCommandLocaleProvider: TextCommandLocaleProvider,
    private val baseContext: LocalizationContextImpl,
) : ClassParameterResolver<TextLocalizationContextResolver, TextLocalizationContext>(TextLocalizationContext::class),
    ICustomResolver<TextLocalizationContextResolver, TextLocalizationContext> {

    override suspend fun resolveSuspend(executable: Executable, event: Event): TextLocalizationContext {
        return when (event) {
            is Interaction -> baseContext.withLocales(guildLocaleProvider.getDiscordLocale(event), userLocaleProvider.getDiscordLocale(event))
            is MessageReceivedEvent -> when {
                event.isFromGuild -> baseContext.withGuildLocale(textCommandLocaleProvider.getDiscordLocale(event))
                else -> baseContext
            }
            else -> throwInternal("Unsupported event type for ${classRef<TextLocalizationContext>()}: ${event.javaClass.name}")
        }
    }
}