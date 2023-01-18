package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.LocalizationContext
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import com.freya02.botcommands.internal.nullIfEmpty
import com.freya02.botcommands.internal.parameters.resolvers.LocalizationContextResolverFactory.LocalizationContextResolver
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@IncludeClasspath
internal class LocalizationContextResolverFactory : ParameterResolverFactory<LocalizationContextResolver, LocalizationContext>(
    LocalizationContextResolver::class,
    LocalizationContext::class
) {
    override fun get(parameter: KParameter): LocalizationContextResolver {
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameter.function, "${LocalizationContext::class.simpleName} parameters must be annotated with @${LocalizationBundle::class.simpleName}")

        return LocalizationContextResolver(LocalizationContextImpl(
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfEmpty(),
            guildLocale = null,
            userLocale = null
        ))
    }

    internal class LocalizationContextResolver(private val baseContext: LocalizationContextImpl) :
        ParameterResolver<LocalizationContextResolver, LocalizationContext>(LocalizationContext::class),
        ICustomResolver<LocalizationContextResolver, LocalizationContext> {

        override suspend fun resolveSuspend(context: BContext, executableInteractionInfo: IExecutableInteractionInfo, event: Event): LocalizationContext {
            return when (event) {
                is Interaction -> baseContext.withLocales(event.guildLocale, event.userLocale)
                is MessageReceivedEvent -> when {
                    event.isFromGuild -> baseContext.withGuildLocale(event.guild.locale)
                    else -> baseContext
                }
                else -> throwInternal("Unsupported event type for localization contexts: ${event.javaClass.name}")
            }
        }
    }
}