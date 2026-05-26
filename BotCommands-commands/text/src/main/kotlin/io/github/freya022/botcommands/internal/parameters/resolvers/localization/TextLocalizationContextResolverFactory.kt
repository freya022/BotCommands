package io.github.freya022.botcommands.internal.parameters.resolvers.localization

import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.typeOf

// Don't require enabled feature, could be used by user's own impl
@ResolverFactory
@RequiresTextCommands
internal class TextLocalizationContextResolverFactory(
    private val localizationService: LocalizationService,
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val messageLocaleProvider: MessageLocaleProvider,
) : TypedParameterResolverFactory(typeOf<TextLocalizationContext>()) {

    override val supportedResolvers = listOf(ICustomResolver::class.java)

    override fun get(request: ResolverRequest) =
        TextLocalizationContextResolver(
            userLocaleProvider,
            guildLocaleProvider,
            messageLocaleProvider,
            LocalizationContextResolverFactories.getBaseLocalizationContext(
                localizationService,
                request.parameter.parameter,
                Interaction::class,
                MessageReceivedEvent::class
            )
        )
}
