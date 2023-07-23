package com.freya02.botcommands.internal.parameters.resolvers.localization

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.core.utils.isSubclassOfAny
import com.freya02.botcommands.api.core.utils.nullIfEmpty
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.TextLocalizationContext
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.api.parameters.ParameterWrapper
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import com.freya02.botcommands.internal.parameters.resolvers.localization.LocalizationContextResolverFactories.getBaseLocalizationContext
import com.freya02.botcommands.internal.utils.ReflectionUtils.function
import com.freya02.botcommands.internal.utils.requireUser
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@ResolverFactory
internal class AppLocalizationContextResolverFactory(private val context: BContext) : ParameterResolverFactory<AppLocalizationContextResolver, AppLocalizationContext>(
    AppLocalizationContextResolver::class,
    AppLocalizationContext::class
) {
    override fun get(parameter: ParameterWrapper) =
        AppLocalizationContextResolver(getBaseLocalizationContext(
            context, parameter, Interaction::class
        ))
}

@ResolverFactory
internal class TextLocalizationContextResolverFactory(private val context: BContext) : ParameterResolverFactory<TextLocalizationContextResolver, TextLocalizationContext>(
    TextLocalizationContextResolver::class.java,
    TextLocalizationContext::class.java
) {
    override fun get(parameter: ParameterWrapper) =
        TextLocalizationContextResolver(getBaseLocalizationContext(
            context, parameter, Interaction::class, MessageReceivedEvent::class
        ))
}

internal object LocalizationContextResolverFactories {
    fun getBaseLocalizationContext(context: BContext, parameterWrapper: ParameterWrapper, vararg requiredEventTypes: KClass<*>): LocalizationContextImpl {
        val parameter = parameterWrapper.parameter ?: throwInternal("Tried to get localization context on a null parameter")
        val parameterFunction = parameter.function
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameterFunction, "${parameter.type.jvmErasure.simpleName} parameters must be annotated with @${LocalizationBundle::class.simpleName}")

        val firstParamErasure = parameterFunction.valueParameters.first().type.jvmErasure
        requireUser(firstParamErasure.isSubclassOfAny(*requiredEventTypes), parameterFunction) {
            "${parameter.type.jvmErasure.simpleName} parameters only works with ${requiredEventTypes.joinToString(" or ")} as the first parameter"
        }

        return LocalizationContextImpl(
            context.getService<LocalizationService>(),
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfEmpty(),
            guildLocale = null,
            userLocale = null
        )
    }
}