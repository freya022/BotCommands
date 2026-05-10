package dev.freya02.botcommands.typesafe.messages.internal

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.text.MessageLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.ConditionalOnClass
import net.dv8tion.jda.api.entities.Message
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass as SpringConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Locale

internal interface MessageLocaleProviderAdapter {
    fun getLocale(message: Message): Locale
}

@BService
@Configuration
@ConditionalOnClass(MessageLocaleProvider::class)
@SpringConditionalOnClass(MessageLocaleProvider::class)
internal open class MessageLocaleProviderAdapterProvider {

    @Bean
    @BService
    open fun messageLocaleProviderAdapter(serviceContainer: ServiceContainer): MessageLocaleProviderAdapter {
        return MessageLocaleProviderAdapterImpl(serviceContainer.getService<MessageLocaleProvider>())
    }

    private class MessageLocaleProviderAdapterImpl(private val provider: MessageLocaleProvider) : MessageLocaleProviderAdapter {

        override fun getLocale(message: Message): Locale {
            return provider.getLocale(message)
        }
    }
}
