package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.utils.classRef
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// spring moment
// need to make a (totally useless) factory for a simple object,
// as ConditionalOnMissingBean doesn't always work
@BService
@Configuration
internal open class DefaultInteractionLocaleProviders internal constructor() {
    private object DefaultUserLocaleProvider : UserLocaleProvider {
        override fun getDiscordLocale(interaction: Interaction): DiscordLocale = interaction.userLocale

        object ExistingSupplierChecker : ConditionalServiceChecker {
            override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
                // Try to get UserLocaleProvider interfaced services, except ours
                // If empty, then the user didn't provide one, in which case we can allow
                //Won't take DefaultUserLocaleProvider into account
                val providers = serviceContainer.getInterfacedServices<UserLocaleProvider>()
                if (providers.isNotEmpty())
                    return "An user supplied ${classRef<UserLocaleProvider>()} is already active (${providers.first().javaClass.simpleNestedName})"

                return null
            }
        }
    }

    private object DefaultGuildLocaleProvider : GuildLocaleProvider {
        override fun getDiscordLocale(interaction: Interaction): DiscordLocale = interaction.guildLocale

        object ExistingSupplierChecker : ConditionalServiceChecker {
            override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
                // Try to get GuildLocaleProvider interfaced services, except ours
                // If empty, then the user didn't provide one, in which case we can allow
                //Won't take DefaultGuildLocaleProvider into account
                val providers = serviceContainer.getInterfacedServices<GuildLocaleProvider>()
                if (providers.isNotEmpty())
                    return "An user supplied ${classRef<GuildLocaleProvider>()} is already active (${providers.first().javaClass.simpleNestedName})"

                return null
            }
        }
    }

    @BService
    @ConditionalService(DefaultUserLocaleProvider.ExistingSupplierChecker::class)
    @Bean
    @ConditionalOnMissingBean(UserLocaleProvider::class)
    internal open fun defaultUserLocaleProvider(): UserLocaleProvider = DefaultUserLocaleProvider

    @BService
    @ConditionalService(DefaultGuildLocaleProvider.ExistingSupplierChecker::class)
    @Bean
    @ConditionalOnMissingBean(GuildLocaleProvider::class)
    internal open fun defaultGuildLocaleProvider(): GuildLocaleProvider = DefaultGuildLocaleProvider
}