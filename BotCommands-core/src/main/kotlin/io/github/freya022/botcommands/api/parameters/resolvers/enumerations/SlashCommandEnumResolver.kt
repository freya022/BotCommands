package io.github.freya022.botcommands.api.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.parameters.Resolvers.toHumanName
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.SlashCommandEnumResolverBuilderImpl
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Entry point to create enum resolver modules for slash commands.
 */
object SlashCommandEnumResolver {
    /**
     * Creates a new builder of an enum resolver module for slash commands.
     *
     * It must be built then registered using [EnumResolverBuilder.with].
     */
    @JvmStatic
    fun <E : Enum<E>> builder(enumType: Class<E>): SlashCommandEnumResolverBuilder<E> {
        return SlashCommandEnumResolverBuilderImpl(enumType)
    }

    /**
     * Creates a new enum resolver module for slash commands.
     *
     * It must be registered using [EnumResolverBuilder.with].
     */
    @JvmStatic
    fun <E : Enum<E>> of(enumType: Class<E>): EnumResolverModule<E> {
        return builder(enumType).build()
    }

    /**
     * Returns values on a per-guild basis.
     */
    fun interface ValuesSupplier<E : Enum<E>> {
        /**
         * @param guild The guild containing the command, `null` for global commands
         */
        fun get(guild: Guild?): Collection<E>
    }
}

/**
 * Register support for slash command parameters.
 *
 * ### Using choices
 *
 * The enum values can be used by enabling predefined choices.
 *
 * ### Localization
 *
 * The choices are localized automatically by using the bundles defined by [BApplicationConfigBuilder.addLocalizations],
 * using a path similar to **my.command.path**.options.**my_option**.choices.**choice_name**.name,
 * as required by [LocalizationFunction].
 *
 * The choice name is produced by the [name function][SlashCommandEnumResolverBuilder.overrideNameFunction],
 * and is then lowercased with spaces modified to underscore by [LocalizationFunction].
 *
 * For example, using the [default name function][toHumanName]:
 *
 * 1. `MY_ENUM_VALUE` (Raw enum name)
 * 2. `My enum value` (Choice name displayed on Discord)
 * 3. `my_enum_value` (Choice name in your localization file)
 */
inline fun <reified E : Enum<E>> EnumResolverBuilder<E>.withSlashCommands(crossinline block: SlashCommandEnumResolverBuilder<E>.() -> Unit = {}): EnumResolverBuilder<E> {
    return with(
        SlashCommandEnumResolver.builder(E::class.java)
            .apply(block)
            .build()
    )
}
