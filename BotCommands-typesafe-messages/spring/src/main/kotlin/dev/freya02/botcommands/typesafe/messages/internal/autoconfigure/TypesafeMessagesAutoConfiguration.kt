package dev.freya02.botcommands.typesafe.messages.internal.autoconfigure

import io.github.freya022.botcommands.internal.core.annotations.InternalComponentScan
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@InternalComponentScan(
    basePackages = [
        "dev.freya02.botcommands.typesafe.messages.api",
        "dev.freya02.botcommands.typesafe.messages.internal",
    ]
)
internal open class TypesafeMessagesAutoConfiguration {

    @Bean
    internal open fun springMessageSourceFactoryProvider(
        context: ApplicationContext,
    ): MessageSourceFactoryPostProcessor {
        return MessageSourceFactoryPostProcessor(context)
    }
}
