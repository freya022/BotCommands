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
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.*
import javax.annotation.CheckReturnValue

/**
 * Utility factories to create commonly used parameter resolvers.
 */
object Resolvers {
    /**
     * Creates an enum resolver for [slash][SlashParameterResolver] commands,
     * as well as [component data][ComponentParameterResolver] and [timeout data][TimeoutParameterResolver].
     *
     * As sharing the same display name for text commands and slash commands would be a bad UX,
     * and their values are not per-guild, text command options are unsupported;
     * however, you can make your own, see [TextParameterResolver].
     *
     * ### Using choices
     *
     * You have to enable [SlashOption.usePredefinedChoices] for the choices to appear on your slash command option.
     *
     * ### Registration
     *
     * The created resolver needs to be registered as a service factory, with [@Resolver][Resolver], for example:
     *
     * ```java
     * @BConfiguration
     * public class EnumResolvers {
     *     // Resolver for DAYS/HOURS/MINUTES (and SECONDS in the test guild), where the displayed name is given by 'Resolvers#toHumanName'
     *     @Resolver
     *     public ParameterResolver<?, ?> timeUnitResolver() {
     *         return Resolvers.enumResolver(
     *             TimeUnit.class,
     *             guild -> Utils.isTestGuild(guild)
     *                 ? EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS)
     *                 : EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)
     *         );
     *     }
     *
     *     ...other resolvers...
     * }
     * ```
     *
     * ### Localization
     *
     * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
     * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
     * as required by [LocalizationFunction].
     *
     * The choice name is produced by [the name function][EnumResolverBuilder.nameFunction],
     * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
     *
     * For example, using the [default name function][toHumanName]:
     *
     * 1. `MY_ENUM_VALUE` (Raw enum name)
     * 2. `My enum value` (Choice name displayed on Discord)
     * 3. `my_enum_value` (Choice name in your localization file)
     *
     * @param e                   The enum type
     * @param guildValuesSupplier Retrieves the values used for slash command choices, for each [Guild]
     *
     * @see toHumanName
     */
    @JvmStatic
    @CheckReturnValue
    fun <E : Enum<E>> enumResolver(e: Class<E>, guildValuesSupplier: EnumValuesSupplier<E>): EnumResolverBuilder<E> {
        return EnumResolverBuilder(e, guildValuesSupplier)
    }

    /**
     * Creates an enum resolver for [slash][SlashParameterResolver] commands,
     * as well as [component data][ComponentParameterResolver] and [timeout data][TimeoutParameterResolver].
     *
     * As sharing the same display name for text commands and slash commands would be a bad UX,
     * and their values are not per-guild, text command options are unsupported;
     * however, you can make your own, see [TextParameterResolver].
     *
     * ### Using choices
     *
     * You have to enable [SlashOption.usePredefinedChoices] for the choices to appear on your slash command option.
     *
     * ### Registration
     *
     * The created resolver needs to be registered as a service factory, with [@Resolver][Resolver], for example:
     *
     * ```java
     * @BConfiguration
     * public class EnumResolvers {
     *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
     *     @Resolver
     *     public ParameterResolver<?, ?> timeUnitResolver() {
     *         return Resolvers.enumResolver(TimeUnit.class, EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)).build();
     *     }
     *
     *     ...other resolvers...
     * }
     * ```
     *
     * ### Localization
     *
     * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
     * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
     * as required by [LocalizationFunction].
     *
     * The choice name is produced by [the name function][EnumResolverBuilder.nameFunction],
     * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
     *
     * For example, using the [default name function][toHumanName]:
     *
     * 1. `MY_ENUM_VALUE` (Raw enum name)
     * 2. `My enum value` (Choice name displayed on Discord)
     * 3. `my_enum_value` (Choice name in your localization file)
     *
     * @param e      The enum type
     * @param values The values used for slash command choices
     *
     * @see toHumanName
     */
    @JvmStatic
    @JvmOverloads
    @CheckReturnValue
    fun <E : Enum<E>> enumResolver(e: Class<E>, values: Collection<E> = EnumSet.allOf(e)): EnumResolverBuilder<E> {
        return EnumResolverBuilder(e, guildValuesSupplier = { values })
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
 * Creates an enum resolver for [slash][SlashParameterResolver] commands,
 * as well as [component data][ComponentParameterResolver] and [timeout data][TimeoutParameterResolver].
 *
 * As sharing the same display name for text commands and slash commands would be a bad UX,
 * and their values are not per-guild, text command options are unsupported;
 * however, you can make your own, see [TextParameterResolver].
 *
 * ### Using choices
 *
 * You have to enable [SlashOption.usePredefinedChoices] for the choices to appear on your slash command option.
 *
 * ### Registration
 *
 * The created resolver needs to be registered as a service factory, with [@Resolver][Resolver], for example:
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
 * ### Localization
 *
 * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
 * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
 * as required by [LocalizationFunction].
 *
 * The choice name is produced by [the name function][nameFunction],
 * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
 *
 * For example, using the [default name function][toHumanName]:
 *
 * 1. `MY_ENUM_VALUE` (Raw enum name)
 * 2. `My enum value` (Choice name displayed on Discord)
 * 3. `my_enum_value` (Choice name in your localization file)
 *
 * @param E            The enum type
 * @param nameFunction Retrieves a human friendly name for the enum value, defaults to [toHumanName]
 *
 * @see toHumanName
 */
inline fun <reified E : Enum<E>> enumResolver(
    vararg values: E = enumValues(),
    noinline nameFunction: (e: E) -> String = { it.toHumanName() },
    block: EnumResolverBuilder<E>.() -> Unit = {}
): ClassParameterResolver<*, E> = Resolvers.enumResolver(E::class.java, values.toCollection(enumSetOf<E>()))
    .nameFunction(nameFunction)
    .apply(block)
    .build()

/**
 * Creates an enum resolver for [slash][SlashParameterResolver] commands,
 * as well as [component data][ComponentParameterResolver] and [timeout data][TimeoutParameterResolver].
 *
 * As sharing the same display name for text commands and slash commands would be a bad UX,
 * and their values are not per-guild, text command options are unsupported;
 * however, you can make your own, see [TextParameterResolver].
 *
 * ### Using choices
 *
 * You have to enable [SlashOption.usePredefinedChoices] for the choices to appear on your slash command option.
 *
 * ### Registration
 *
 * The created resolver needs to be registered as a service factory, with [@Resolver][Resolver], for example:
 *
 * ```kt
 * @BConfiguration
 * object EnumResolvers {
 *     // Resolver for DAYS/HOURS/MINUTES (and SECONDS in the test guild), where the displayed name is given by 'Resolvers.Enum#toHumanName'
 *     @Resolver
 *     fun timeUnitResolver() = enumResolver<TimeUnit> { guild ->
 *         if (guild.isTestGuild()) {
 *             enumSetOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS)
 *         } else {
 *             enumSetOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)
 *         }
 *     }
 *
 *     ...other resolvers...
 * }
 * ```
 *
 * ### Localization
 *
 * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
 * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
 * as required by [LocalizationFunction].
 *
 * The choice name is produced by [the name function][nameFunction],
 * and is then lowercase with spaces modified to underscore by [LocalizationFunction].
 *
 * For example, using the [default name function][toHumanName]:
 *
 * 1. `MY_ENUM_VALUE` (Raw enum name)
 * 2. `My enum value` (Choice name displayed on Discord)
 * 3. `my_enum_value` (Choice name in your localization file)
 *
 * @param E                   The enum type
 * @param guildValuesSupplier Retrieves the values used for slash command choices, for each [Guild]
 * @param nameFunction        Retrieves a human friendly name for the enum value, defaults to [toHumanName]
 *
 * @see toHumanName
 */
inline fun <reified E : Enum<E>> enumResolver(
    guildValuesSupplier: EnumValuesSupplier<E>,
    noinline nameFunction: (e: E) -> String = { it.toHumanName() },
    block: EnumResolverBuilder<E>.() -> Unit = {}
): ClassParameterResolver<*, E> = Resolvers.enumResolver(E::class.java, guildValuesSupplier)
    .nameFunction(nameFunction)
    .apply(block)
    .build()

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
 *         override suspend fun resolveSuspend(executable: Executable, event: Event): MyCustomLocalization {
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