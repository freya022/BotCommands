package dev.freya02.botcommands.typesafe.messages.internal.autoconfigure

import dev.freya02.botcommands.typesafe.messages.internal.processor.MessageSourceFactoryPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
internal open class TypesafeMessagesAutoConfiguration {

    @Bean
    internal open fun springMessageSourceFactoryProvider(
        context: ApplicationContext,
    ): MessageSourceFactoryPostProcessor {
        return MessageSourceFactoryPostProcessor(context)
    }
}
