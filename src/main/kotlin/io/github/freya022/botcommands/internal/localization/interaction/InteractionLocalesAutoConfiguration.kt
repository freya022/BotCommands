package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class InteractionLocalesAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(UserLocaleProvider::class)
    open fun userLocaleProvider(): UserLocaleProvider {
        return DefaultUserLocaleProvider
    }

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(GuildLocaleProvider::class)
    open fun guildLocaleProvider(): GuildLocaleProvider {
        return DefaultGuildLocaleProvider
    }
}
