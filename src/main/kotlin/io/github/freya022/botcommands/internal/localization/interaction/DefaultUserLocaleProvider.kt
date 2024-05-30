package io.github.freya022.botcommands.internal.localization.interaction

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.utils.classRef
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@BService
@ConditionalService(DefaultUserLocaleProvider.ExistingSupplierChecker::class)
@ConditionalOnMissingBean(UserLocaleProvider::class)
internal class DefaultUserLocaleProvider internal constructor() : UserLocaleProvider {
    override fun getLocale(interaction: Interaction): DiscordLocale = interaction.userLocale

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
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