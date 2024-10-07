package doc.kotlin.examples.parameters

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.reflect.findAnnotation
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

@InterfacedService(acceptMultiple = false)
interface GuildSettingsService {
    fun getGuildLocale(guildId: Long): Locale
}

@BService
class GuildSettingsServiceImpl : GuildSettingsService {
    override fun getGuildLocale(guildId: Long): Locale {
        TODO("Not yet implemented")
    }
}

class MyCustomLocalization(
    private val localization: Localization,
    private val prefix: String?
) {
    fun localize(key: String, vararg args: Pair<String, Any?>): String {
        TODO("construct key from prefix + key, get template, and localize")
    }
}

@BConfiguration
object MyCustomLocalizationResolverProvider {
    // The parameter resolver, which will be created once per parameter
    class MyCustomLocalizationResolver(
        private val localizationService: LocalizationService,
        private val guildSettingsService: GuildSettingsService,
        private val bundleName: String,
        private val prefix: String?
    ) : ClassParameterResolver<MyCustomLocalizationResolver, MyCustomLocalization>(MyCustomLocalization::class),
        ICustomResolver<MyCustomLocalizationResolver, MyCustomLocalization> {

        // Called when a command is used
        override suspend fun resolveSuspend(option: Option, event: Event): MyCustomLocalization {
            return if (event is Interaction) {
                val guild = event.guild
                    ?: throw IllegalStateException("Cannot get localization outside of guilds")
                // The root localization file not existing isn't an issue on production
                val localization = localizationService.getInstance(bundleName, guildSettingsService.getGuildLocale(guild.idLong))
                    ?: throw IllegalArgumentException("No root bundle exists for '$bundleName'")

                // Return resolved object
                MyCustomLocalization(localization, prefix)
            } else {
                throw UnsupportedOperationException("Unsupported event: ${event.javaClass.simpleNestedName}")
            }
        }
    }

    // Service factory returning a resolver factory
    // The returned factory is used on each command/handler parameter of type "MyCustomLocalization",
    // which is the same type as what MyCustomLocalizationResolver returns
    @ResolverFactory
    fun myCustomLocalizationResolverProvider(localizationService: LocalizationService, guildSettingsService: GuildSettingsService) = resolverFactory { request ->
        // Find @LocalizationBundle on the parameter
        val bundle = request.parameter.findAnnotation<LocalizationBundle>()
            ?: throw IllegalArgumentException("Parameter ${request.parameter} must be annotated with LocalizationBundle")

        // Return our resolver for that parameter
        MyCustomLocalizationResolver(localizationService, guildSettingsService, bundle.value, bundle.prefix.nullIfBlank())
    }
}