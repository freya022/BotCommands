package io.github.freya022.botcommands.internal.localization.text

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import io.github.freya022.botcommands.internal.utils.classRef
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// spring moment
// need to make a (totally useless) factory for a simple object,
// as ConditionalOnMissingBean doesn't always work
@BService
@Configuration
internal open class DefaultTextCommandLocaleProviderProvider internal constructor() {
    private object DefaultTextCommandLocaleProvider : TextCommandLocaleProvider {
        override fun getDiscordLocale(event: MessageReceivedEvent): DiscordLocale = event.guild.locale

        object ExistingSupplierChecker : ConditionalServiceChecker {
            override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
                // Try to get TextCommandLocaleProvider interfaced services, except ours
                // If empty, then the user didn't provide one, in which case we can allow
                //Won't take DefaultTextCommandLocaleProvider into account
                val providers = serviceContainer.getInterfacedServices<TextCommandLocaleProvider>()
                if (providers.isNotEmpty())
                    return "An user supplied ${classRef<TextCommandLocaleProvider>()} is already active (${providers.first().javaClass.simpleNestedName})"

                return null
            }
        }
    }

    @BService
    @ConditionalService(DefaultTextCommandLocaleProvider.ExistingSupplierChecker::class)
    @Bean
    @ConditionalOnMissingBean(TextCommandLocaleProvider::class)
    open fun defaultTextCommandLocaleProvider(): TextCommandLocaleProvider = DefaultTextCommandLocaleProvider
}