package dev.freya02.botcommands.typesafe.messages.internal.processor

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.reflect.ClassGraphProcessorProvider
import io.github.freya022.botcommands.api.core.reflect.annotations.ExperimentalReflectionApi
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor

@OptIn(ExperimentalReflectionApi::class)
internal class MessageSourceFactoryClassGraphProcessorProvider : ClassGraphProcessorProvider {

    override fun getProcessors(config: BConfig): Collection<ClassGraphProcessor> {
        return listOf(MessageSourceFactoryClassGraphProcessor)
    }
}
