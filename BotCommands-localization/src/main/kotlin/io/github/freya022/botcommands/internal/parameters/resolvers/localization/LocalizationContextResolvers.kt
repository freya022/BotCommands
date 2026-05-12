package io.github.freya022.botcommands.internal.parameters.resolvers.localization

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.localization.LocalizationContextImpl
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.Interaction

internal class AppLocalizationContextResolver(
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val baseContext: LocalizationContextImpl,
) : ClassParameterResolver<AppLocalizationContextResolver, AppLocalizationContext>(AppLocalizationContext::class),
    ICustomResolver<AppLocalizationContextResolver, AppLocalizationContext> {

    override suspend fun resolveSuspend(option: Option, event: Event): AppLocalizationContext {
        return when (event) {
            is Interaction -> baseContext.withLocales(guildLocaleProvider.getLocale(event), userLocaleProvider.getLocale(event))
            //MessageReceivedEvent does not provide user locale
            else -> throwInternal("Unsupported event type for ${classRef<AppLocalizationContext>()}: ${event.javaClass.name}")
        }
    }
}
