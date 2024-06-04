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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@BService
@ConditionalService(DefaultTextCommandLocaleProvider.ExistingSupplierChecker::class)
@ConditionalOnMissingBean(TextCommandLocaleProvider::class)
internal class DefaultTextCommandLocaleProvider : TextCommandLocaleProvider {
    override fun getLocale(event: MessageReceivedEvent): Locale = event.guild.locale.toLocale()

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
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