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
import dev.freya02.botcommands.typesafe.messages.internal.utils.require
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.utils.superErasureAt
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

@BService
internal object PostLoadValidator {

    @BEventListener
    @Suppress("UNCHECKED_CAST")
    fun onPostLoad(event: PostLoadEvent, localizationService: LocalizationService, factories: List<IMessageSourceFactory<*>>) {
        factories.forEach { factory ->
            val factoryType = factory::class

            val bundleName = factory.bundleName
            val localization = localizationService.getInstance(bundleName, Locale.ROOT)
            require(localization != null, ::NoSuchBundleException) {
                "No root localization bundle named '$bundleName' exists for ${factoryType.shortQualifiedName}"
            }

            val sourceType = factoryType.superErasureAt<IMessageSourceFactory<*>>(0).jvmErasure as KClass<out IMessageSource>

            MessageSourceGenerator.getImplementableFunctions(sourceType).forEach { function ->
                val localizedContent = function.findAnnotation<LocalizedContent>()
                    ?: error("Function was said to be implementable but does not have @LocalizedContent")
                val templateKey = localizedContent.templateKey

                val template = localization[templateKey]
                require(template != null, ::NoSuchTemplateKeyException) {
                    "No template key '$templateKey' exists in the root bundle '$bundleName' for ${function.getSignature(qualifiedClass = true, source = false)}"
                }

                val formattableArguments = template.arguments
                // Check all template arguments are present in parameters
                val templateArgumentParameters = LocalizedContentFunctionGenerator.getTemplateArgumentParameters(function)
                templateArgumentParameters.forEach { parameter ->
                    val expectedArgName = LocalizedContentFunctionGenerator.getTemplateArgumentParameterName(parameter)!!
                    require(formattableArguments.any { it.argumentName == expectedArgName }, ::UnmappedParameterException) {
                        "Template key '$templateKey' is missing argument '$expectedArgName' required by ${function.getSignature(qualifiedClass = true, source = false)}"
                    }
                }

                // Check all parameters maps to a template argument
                formattableArguments.forEach { argument ->
                    val expectedParameterName = LocalizedContentFunctionGenerator.getParameterTemplateArgumentName(argument)
                    val hasParameter = templateArgumentParameters.any { it.name == expectedParameterName }
                    require(hasParameter, ::UnmappedTemplateArgumentException) {
                        "Template key '$templateKey' expects a parameter named '$expectedParameterName' in ${function.getSignature(qualifiedClass = true, source = false)}"
                    }
                }
            }
        }
    }
}
