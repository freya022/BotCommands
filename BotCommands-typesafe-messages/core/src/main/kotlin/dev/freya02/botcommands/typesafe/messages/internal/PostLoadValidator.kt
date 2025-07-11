package dev.freya02.botcommands.typesafe.messages.internal

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchBundleException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.NoSuchTemplateKeyException
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

                require(localization[templateKey] != null, ::NoSuchTemplateKeyException) {
                    "No template key '$templateKey' exists in the root bundle '$bundleName' for ${function.getSignature(qualifiedClass = true, source = false)}"
                }
            }
        }
    }
}
