package io.github.freya022.botcommands.internal.parameters.resolvers.localization

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isSubclassOfAny
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.internal.localization.LocalizationContextImpl
import io.github.freya022.botcommands.internal.parameters.resolvers.localization.LocalizationContextResolverFactories.getBaseLocalizationContext
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.requireUser
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

@ResolverFactory
internal class AppLocalizationContextResolverFactory(
    private val localizationService: LocalizationService
) : TypedParameterResolverFactory<AppLocalizationContextResolver>(AppLocalizationContextResolver::class, typeOf<AppLocalizationContext>()) {
    override fun get(parameter: ParameterWrapper) =
        AppLocalizationContextResolver(getBaseLocalizationContext(
            localizationService, parameter, Interaction::class
        ))
}

@ResolverFactory
internal class TextLocalizationContextResolverFactory(
    private val localizationService: LocalizationService
) : TypedParameterResolverFactory<TextLocalizationContextResolver>(TextLocalizationContextResolver::class, typeOf<TextLocalizationContext>()) {
    override fun get(parameter: ParameterWrapper) =
        TextLocalizationContextResolver(getBaseLocalizationContext(
            localizationService, parameter, Interaction::class, MessageReceivedEvent::class
        ))
}

internal object LocalizationContextResolverFactories {
    fun getBaseLocalizationContext(localizationService: LocalizationService, parameterWrapper: ParameterWrapper, vararg requiredEventTypes: KClass<*>): LocalizationContextImpl {
        val parameter = parameterWrapper.parameter ?: throwInternal("Tried to get localization context on a null parameter")
        val parameterFunction = parameter.function
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameterFunction, "${parameter.type.jvmErasure.simpleName} parameters must be annotated with ${annotationRef<LocalizationBundle>()}")

        val firstParamErasure = parameterFunction.valueParameters.first().type.jvmErasure
        requireUser(firstParamErasure.isSubclassOfAny(*requiredEventTypes), parameterFunction) {
            "${parameter.type.jvmErasure.simpleName} parameters only works with ${requiredEventTypes.joinToString(" or ")} as the first parameter"
        }

        return LocalizationContextImpl(
            localizationService,
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfBlank(),
            _guildLocale = null,
            _userLocale = null
        )
    }
}