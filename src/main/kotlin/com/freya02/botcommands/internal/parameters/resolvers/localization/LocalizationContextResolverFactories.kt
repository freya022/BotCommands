package com.freya02.botcommands.internal.parameters.resolvers.localization

import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.TextLocalizationContext
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.isSubclassOfAny
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import com.freya02.botcommands.internal.nullIfEmpty
import com.freya02.botcommands.internal.parameters.resolvers.localization.LocalizationContextResolverFactories.getBaseLocalizationContext
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@IncludeClasspath
internal class AppLocalizationContextResolverFactory : ParameterResolverFactory<AppLocalizationContextResolver, AppLocalizationContext>(
    AppLocalizationContextResolver::class,
    AppLocalizationContext::class
) {
    override fun get(parameter: KParameter) =
        AppLocalizationContextResolver(getBaseLocalizationContext(
            parameter, Interaction::class
        ))
}

@IncludeClasspath
internal class TextLocalizationContextResolverFactory : ParameterResolverFactory<TextLocalizationContextResolver, TextLocalizationContext>(
    TextLocalizationContextResolver::class.java,
    TextLocalizationContext::class.java
) {
    override fun get(parameter: KParameter) =
        TextLocalizationContextResolver(getBaseLocalizationContext(
            parameter, Interaction::class, MessageReceivedEvent::class
        ))
}

internal object LocalizationContextResolverFactories {
    fun getBaseLocalizationContext(parameter: KParameter, vararg requiredEventTypes: KClass<*>): LocalizationContextImpl {
        val parameterFunction = parameter.function
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameterFunction, "${parameter.type.jvmErasure.simpleName} parameters must be annotated with @${LocalizationBundle::class.simpleName}")

        val firstParamErasure = parameterFunction.valueParameters.first().type.jvmErasure
        requireUser(firstParamErasure.isSubclassOfAny(*requiredEventTypes), parameterFunction) {
            "${parameter.type.jvmErasure.simpleName} parameters only works with ${requiredEventTypes.joinToString(", ")} events"
        }

        return LocalizationContextImpl(
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfEmpty(),
            guildLocale = null,
            userLocale = null
        )
    }
}