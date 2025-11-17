package dev.freya02.botcommands.typesafe.messages.internal

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchBundleException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchTemplateKeyException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedParameterException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnmappedTemplateArgumentException
import dev.freya02.botcommands.typesafe.messages.internal.codegen.LocalizedContentFunctionGenerator
import dev.freya02.botcommands.typesafe.messages.internal.codegen.MessageSourceGenerator
import dev.freya02.botcommands.typesafe.messages.internal.exceptions.throwInternal
import dev.freya02.botcommands.typesafe.messages.internal.utils.require
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.utils.superErasureAt
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

@BService
internal object PostLoadValidator {

    @BEventListener
    fun onPostLoad(event: PostLoadEvent, localizationService: LocalizationService, factories: List<IMessageSourceFactory<*>>) {
        for (factory in factories) {
            context(localizationService) {
                checkFactory(factory)
            }
        }
    }

    context(localizationService: LocalizationService)
    private fun checkFactory(factory: IMessageSourceFactory<*>) {
        val bundleName = factory.bundleName
        val rootBundle = retrieveRootBundle(factory)
        val localizedBundles = retrieveLocalizedBundles(factory)

        @Suppress("UNCHECKED_CAST")
        val sourceType = factory::class.superErasureAt<IMessageSourceFactory<*>>(0).jvmErasure as KClass<out IMessageSource>
        for (function in MessageSourceGenerator.getImplementableFunctions(sourceType)) {
            context(bundleName, rootBundle, localizedBundles) {
                checkLocalizedTemplate(function)
            }
        }
    }

    context(bundleName: String, rootBundle: Localization, localizedBundles: Map<Locale, Localization>)
    private fun checkLocalizedTemplate(function: KFunction<*>) {
        val localizedContent = function.findAnnotation<LocalizedContent>()
            ?: error("Function was said to be implementable but does not have @LocalizedContent")
        val templateKey = localizedContent.templateKey

        val rootTemplate = rootBundle[templateKey]
        require(rootTemplate != null, ::NoSuchTemplateKeyException) {
            "No template key '$templateKey' exists in the root bundle '$bundleName' for ${function.getSignature(qualifiedClass = true, source = false)}"
        }

        val templateArgumentParameters = LocalizedContentFunctionGenerator.getTemplateArgumentParameters(function)
        // Check all template arguments are present in parameters
        for (parameter in templateArgumentParameters) {
            val expectedArgName = LocalizedContentFunctionGenerator.getTemplateArgumentParameterName(parameter)!!
            require(rootTemplate.arguments.any { it.argumentName == expectedArgName }, ::UnmappedParameterException) {
                "Template key '$templateKey' from root bundle '$bundleName' is missing argument '$expectedArgName' required by ${function.getSignature(qualifiedClass = true, source = false)}"
            }

            // Check specializations
            for ((locale, bundle) in localizedBundles) {
                val localizedTemplate = bundle[templateKey]
                    ?: throwInternal("Template '$templateKey' could not be found from bundle '$bundleName' with locale ${locale.toLanguageTag()} but root template was, templates were probably not merged")

                require(localizedTemplate.arguments.any { it.argumentName == expectedArgName }, ::UnmappedParameterException) {
                    "Template key '$templateKey' from bundle '$bundleName' with locale ${locale.toLanguageTag()} is missing argument '$expectedArgName' required by ${function.getSignature(qualifiedClass = true, source = false)}"
                }
            }
        }

        // Check all parameters maps to a template argument
        for (argument in rootTemplate.arguments) {
            val expectedParameterName = LocalizedContentFunctionGenerator.getParameterTemplateArgumentName(argument)
            val hasParameter = templateArgumentParameters.any { it.name == expectedParameterName }
            require(hasParameter, ::UnmappedTemplateArgumentException) {
                "Template key '$templateKey' from root bundle '$bundleName' expects a parameter named '$expectedParameterName' in ${function.getSignature(qualifiedClass = true, source = false)}"
            }
        }
    }

    context(localizationService: LocalizationService)
    private fun retrieveRootBundle(factory: IMessageSourceFactory<*>): Localization {
        val bundleName = factory.bundleName
        val localization = localizationService.getInstance(bundleName, Locale.ROOT)
        require(localization != null, ::NoSuchBundleException) {
            val factoryTypeStr = factory::class.shortQualifiedName
            "No root localization bundle named '$bundleName' exists for $factoryTypeStr"
        }

        return localization
    }

    context(localizationService: LocalizationService)
    private fun retrieveLocalizedBundles(factory: IMessageSourceFactory<*>): Map<Locale, Localization> {
        val localeBundles = mutableMapOf<Locale, Localization>()

        for (locale in factory.locales) {
            val localizedBundle = localizationService.getInstance(factory.bundleName, locale)
            require(localizedBundle != null, ::NoSuchBundleException) {
                val languageTag = locale.toLanguageTag()
                val factoryTypeStr = factory::class.shortQualifiedName
                "No localized bundle named '${factory.bundleName}' with locale '${languageTag}' exists for $factoryTypeStr"
            }
            localeBundles[locale] = localizedBundle
        }

        return localeBundles
    }
}
