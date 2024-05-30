package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.internal.utils.classRef
import net.dv8tion.jda.api.interactions.Interaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@BService
@ConditionalService(DefaultGuildLocaleProvider.ExistingSupplierChecker::class)
@ConditionalOnMissingBean(GuildLocaleProvider::class)
internal class DefaultGuildLocaleProvider internal constructor() : GuildLocaleProvider {
    override fun getLocale(interaction: Interaction): Locale = interaction.guildLocale.toLocale()

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
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