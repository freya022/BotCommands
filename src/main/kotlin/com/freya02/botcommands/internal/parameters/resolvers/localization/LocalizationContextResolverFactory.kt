package com.freya02.botcommands.internal.parameters.resolvers.localization

import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.LocalizationContext
import com.freya02.botcommands.api.localization.context.TextLocalizationContext
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import com.freya02.botcommands.internal.nullIfEmpty
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

@IncludeClasspath
internal class LocalizationContextResolverFactory : ParameterResolverFactory<AppLocalizationContextResolver, AppLocalizationContext>(
    AppLocalizationContextResolver::class,
    AppLocalizationContext::class
) {
    override fun get(parameter: KParameter): AppLocalizationContextResolver {
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameter.function, "${LocalizationContext::class.simpleName} parameters must be annotated with @${LocalizationBundle::class.simpleName}")

        return AppLocalizationContextResolver(LocalizationContextImpl(
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfEmpty(),
            guildLocale = null,
            userLocale = null
        ))
    }
}

@IncludeClasspath
internal class TextLocalizationContextResolverFactory : ParameterResolverFactory<TextLocalizationContextResolver, TextLocalizationContext>(
    TextLocalizationContextResolver::class.java,
    TextLocalizationContext::class.java
) {
    override fun get(parameter: KParameter): TextLocalizationContextResolver {
        val annotation = parameter.findAnnotation<LocalizationBundle>()
            ?: throwUser(parameter.function, "${LocalizationContext::class.simpleName} parameters must be annotated with @${LocalizationBundle::class.simpleName}")

        return TextLocalizationContextResolver(LocalizationContextImpl(
            localizationBundle = annotation.value,
            localizationPrefix = annotation.prefix.nullIfEmpty(),
            guildLocale = null,
            userLocale = null
        ))
    }
}