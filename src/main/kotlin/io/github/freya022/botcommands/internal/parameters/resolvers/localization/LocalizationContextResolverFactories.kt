package io.github.freya022.botcommands.internal.parameters.resolvers.localization

import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isSubclassOfAny
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.TextLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.internal.localization.LocalizationContextImpl
import io.github.freya022.botcommands.internal.parameters.resolvers.localization.LocalizationContextResolverFactories.getBaseLocalizationContext
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.findAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

// Don't require enabled feature, could be used by user's own impl
@ResolverFactory
internal class AppLocalizationContextResolverFactory(
    private val localizationService: LocalizationService,
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
) : TypedParameterResolverFactory<AppLocalizationContextResolver>(AppLocalizationContextResolver::class, typeOf<AppLocalizationContext>()) {
    override fun get(request: ResolverRequest) =
        AppLocalizationContextResolver(
            userLocaleProvider,
            guildLocaleProvider,
            getBaseLocalizationContext(localizationService, request.parameter.parameter, Interaction::class)
        )
}

// Don't require enabled feature, could be used by user's own impl
@ResolverFactory
internal class TextLocalizationContextResolverFactory(
    private val localizationService: LocalizationService,
    private val userLocaleProvider: UserLocaleProvider,
    private val guildLocaleProvider: GuildLocaleProvider,
    private val textCommandLocaleProvider: TextCommandLocaleProvider,
) : TypedParameterResolverFactory<TextLocalizationContextResolver>(TextLocalizationContextResolver::class, typeOf<TextLocalizationContext>()) {
    override fun get(request: ResolverRequest) =
        TextLocalizationContextResolver(
            userLocaleProvider,
            guildLocaleProvider,
            textCommandLocaleProvider,
            getBaseLocalizationContext(localizationService, request.parameter.parameter, Interaction::class, MessageReceivedEvent::class)
        )
}

internal object LocalizationContextResolverFactories {
    fun getBaseLocalizationContext(localizationService: LocalizationService, parameter: KParameter, vararg requiredEventTypes: KClass<*>): LocalizationContextImpl {
        val parameterFunction = parameter.function
        val annotation = parameter.findAnnotationRecursive<LocalizationBundle>()
            ?: throwArgument(parameterFunction, "${parameter.type.jvmErasure.simpleName} parameters must be annotated with ${annotationRef<LocalizationBundle>()}")

        val firstParamErasure = parameterFunction.valueParameters.first().type.jvmErasure
        requireAt(firstParamErasure.isSubclassOfAny(*requiredEventTypes), parameterFunction) {
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