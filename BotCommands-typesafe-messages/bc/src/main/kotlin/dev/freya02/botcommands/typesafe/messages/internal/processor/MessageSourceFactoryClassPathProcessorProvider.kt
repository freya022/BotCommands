package dev.freya02.botcommands.typesafe.messages.internal.processor

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.reflect.annotations.ExperimentalReflectionApi
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.core.ClassPathProcessorProvider

@OptIn(ExperimentalReflectionApi::class)
internal class MessageSourceFactoryClassPathProcessorProvider : ClassPathProcessorProvider {

    override fun getProcessors(config: BConfig): Collection<ClassPathProcessor> {
        return listOf(MessageSourceFactoryClassPathProcessor)
    }
}
