package dev.freya02.botcommands.typesafe.messages.internal

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.ConditionalOnClass
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass as SpringConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

internal interface TextCommandLocaleProviderAdapter {
    fun getLocale(event: MessageReceivedEvent): Locale
}

@BService
@Configuration
@ConditionalOnClass(TextCommandLocaleProvider::class)
@SpringConditionalOnClass(TextCommandLocaleProvider::class)
internal open class TextCommandLocaleProviderAdapterProvider {

    @Bean
    @BService
    open fun textCommandLocaleProviderAdapter(serviceContainer: ServiceContainer): TextCommandLocaleProviderAdapter {
        return TextCommandLocaleProviderAdapterImpl(serviceContainer.getService<TextCommandLocaleProvider>())
    }

    private class TextCommandLocaleProviderAdapterImpl(private val provider: TextCommandLocaleProvider) : TextCommandLocaleProviderAdapter {

        override fun getLocale(event: MessageReceivedEvent): Locale {
            return provider.getLocale(event)
        }
    }
}
