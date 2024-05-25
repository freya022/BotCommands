package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.Resolvers.toHumanName
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*

/**
 * Utility factories to create commonly used parameter resolvers.
 */
object Resolvers {
    /**
     * Creates an enum resolver for [text][TextParameterResolver]/[slash][SlashParameterResolver] commands,
     * as well as [component data][ComponentParameterResolver].
     *
     * Text command options are case-insensitive.
     *
     * The created resolver needs to be registered either by using a service factory with [@Resolver][Resolver],
     * such as:
     *
     * ```java
     * @BConfiguration
     * public class EnumResolvers {
     *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
     *     @Resolver
     *     public ParameterResolver<?, ?> timeUnitResolver() {
     *         return Resolvers.enumResolver(TimeUnit.class, EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES));
     *     }
     *
     *     ...other resolvers...
     * }
     * ```
     *
     * **Note:** You have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
     *
     * ### Localization
     *
     * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
     * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
     * as required by [LocalizationFunction].
     *
     * The choice name is produced by [nameFunction],
     * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
     *
     * For example, using the [default name function][toHumanName]:
     *
     * 1. `MY_ENUM_VALUE` (Raw enum name)
     * 2. `My enum value` (Choice name displayed on Discord)
     * 3. `my_enum_value` (Choice name in your localization file)
     *
     * @param values              The accepted enumeration values
     * @param guildValuesSupplier The function retrieving the enum values depending on the [Guild]
     * @param nameFunction        The function transforming the enum value into the display name, uses [toHumanName] by default
     *
     * @see toHumanName
     */
    @JvmStatic
    @JvmOverloads //TODO use a builder pattern
    fun <E : Enum<E>> enumResolver(
        e: Class<E>,
        values: Collection<E> = e.enumConstants.toCollection(EnumSet.noneOf(e)),
        guildValuesSupplier: EnumValuesSupplier<E> = EnumValuesSupplier { values },
        nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }
    ): ClassParameterResolver<*, E> {
        return EnumResolver(e, values, guildValuesSupplier, nameFunction)
    }

    /**
     * Convert an enum to a more human-friendly name.
     *
     * This takes the enum value's name and capitalizes it, while replacing underscores with spaces, for example,
     * `MY_ENUM_VALUE` -> `Enum value name`.
     */
    @JvmStatic
    @JvmOverloads
    fun toHumanName(value: Enum<*>, locale: Locale = Locale.ROOT): String {
        return value.name.lowercase(locale)
            .replace('_', ' ')
            .replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Creates an enum resolver for [text][TextParameterResolver]/[slash][SlashParameterResolver] commands,
 * as well as [component data][ComponentParameterResolver].
 *
 * Text command options are case-insensitive.
 *
 * The created resolver needs to be registered either by using a service factory with [@Resolver][Resolver],
 * such as:
 *
 * ```kt
 * @BConfiguration
 * object EnumResolvers {
 *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers.Enum#toHumanName'
 *     @Resolver
 *     fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)
 *
 *     ...other resolvers...
 * }
 * ```
 *
 * **Note:** You have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
 *
 * ### Localization
 *
 * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
 * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
 * as required by [LocalizationFunction].
 *
 * The choice name is produced by [nameFunction],
 * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
 *
 * For example, using the [default name function][toHumanName]:
 *
 * 1. `MY_ENUM_VALUE` (Raw enum name)
 * 2. `My enum value` (Choice name displayed on Discord)
 * 3. `my_enum_value` (Choice name in your localization file)
 *
 * @param values              The accepted enumeration values
 * @param guildValuesSupplier The function retrieving the enum values depending on the [Guild]
 * @param nameFunction        The function transforming the enum value into the display name, uses [toHumanName] by default
 *
 * @see toHumanName
 */
inline fun <reified E : Enum<E>> enumResolver(
    vararg values: E = enumValues(),
    guildValuesSupplier: EnumValuesSupplier<E> = defaultEnumValuesSupplier<E>(values.toCollection(enumSetOf<E>())),
    noinline nameFunction: (e: E) -> String = { it.toHumanName() }
): ClassParameterResolver<*, E> = Resolvers.enumResolver(E::class.java, values.toCollection(enumSetOf<E>()), guildValuesSupplier, nameFunction)

// Cannot directly inline this. It is used as a way to not make a copy of the values for every guild
@PublishedApi
internal inline fun <reified E : Enum<E>> defaultEnumValuesSupplier(valueCollection: Collection<E>) =
    EnumValuesSupplier { valueCollection }

/**
 * Convert an enum to a more human-friendly name.
 *
 * This takes the enum value's name and capitalizes it, while replacing underscores with spaces, for example,
 * `MY_ENUM_VALUE` -> `Enum value name`.
 */
fun Enum<*>.toHumanName(locale: Locale = Locale.ROOT): String = toHumanName(this, locale)

/**
 * Creates a [parameter resolver factory][ParameterResolverFactory] from the provided resolver [producer].
 *
 * The [producer] is called for each function parameter with the exact [R] type.
 *
 * This should be returned in a service factory, using [@ResolverFactory][ResolverFactory].
 *
 * Example using a custom localization service:
 * ```kt
 * @BConfiguration
 * object MyCustomLocalizationResolverProvider {
 *     // The parameter resolver, which will be created once per parameter
 *     class MyCustomLocalizationResolver(
 *         private val localizationService: LocalizationService,
 *         private val guildSettingsService: GuildSettingsService,
 *         private val bundleName: String,
 *         private val prefix: String?
 *     ) : ClassParameterResolver<MyCustomLocalizationResolver, MyCustomLocalization>(MyCustomLocalization::class),
 *         ICustomResolver<MyCustomLocalizationResolver, MyCustomLocalization> {
 *
 *         // Called when a command is used
 *         override suspend fun resolveSuspend(info: IExecutableInteractionInfo, event: Event): MyCustomLocalization {
 *             return if (event is Interaction) {
 *                 val guild = event.guild
 *                     ?: throw IllegalStateException("Cannot get localization outside of guilds")
 *                 // The root localization file not existing isn't an issue on production
 *                 val localization = localizationService.getInstance(bundleName, guildSettingsService.getGuildLocale(guild.idLong))
 *                     ?: throw IllegalArgumentException("No root bundle exists for '$bundleName'")
 *
 *                 // Return resolved object
 *                 MyCustomLocalization(localization, prefix)
 *             } else {
 *                 throw UnsupportedOperationException("Unsupported event: ${event.javaClass.simpleNestedName}")
 *             }
 *         }
 *     }
 *
 *     // Service factory returning a resolver factory
 *     // The returned factory is used on each command/handler parameter of type "MyCustomLocalization",
 *     // which is the same type as what MyCustomLocalizationResolver returns
 *     @ResolverFactory
 *     fun myCustomLocalizationResolverProvider(localizationService: LocalizationService, guildSettingsService: GuildSettingsService) = resolverFactory { parameter ->
 *         // Find @LocalizationBundle on the parameter
 *         val bundle = parameter.parameter.findAnnotation<LocalizationBundle>()
 *             ?: throw IllegalArgumentException("Parameter ${parameter.parameter} must be annotated with LocalizationBundle")
 *
 *         // Return our resolver for that parameter
 *         MyCustomLocalizationResolver(localizationService, guildSettingsService, bundle.value, bundle.prefix.nullIfBlank())
 *     }
 * }
 * ```
 *
 * @param producer Function providing a [resolver][ParameterResolver] for the provided function parameter
 * @param T Type of the produced parameter resolver
 * @param R Type of the object returned by the resolver
 *
 * @see ParameterResolverFactory
 * @see ParameterResolver
 */
inline fun <reified T : ParameterResolver<T, R>, reified R : Any> resolverFactory(crossinline producer: (request: ResolverRequest) -> T): ParameterResolverFactory<T> {
    return object : TypedParameterResolverFactory<T>(T::class, R::class) {
        override fun get(request: ResolverRequest): T = producer(request)
    }
}