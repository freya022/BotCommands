package com.freya02.botcommands.internal.parameters.resolvers.localization

import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.LocalizationContext
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.localization.LocalizationContextImpl
import com.freya02.botcommands.internal.nullIfEmpty
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
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
}