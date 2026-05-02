package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplierProvider
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.TopLevelSlashCommandBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Declares this function as a slash command,
 * additional properties can be set with [@TopLevelSlashCommandData][TopLevelSlashCommandData]
 * and [@SlashCommandGroupData][SlashCommandGroupData].
 *
 * See the [Discord docs](https://discord.com/developers/docs/interactions/application-commands.subcommands-and-subcommand-groups)
 * on which paths are allowed.
 *
 * ### Additional annotations
 * Additional data can be set once **per subcommand group** with [@SlashCommandGroupData][SlashCommandGroupData].
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Command][Command].
 * - If you have subcommands,
 * [@TopLevelSlashCommandData][TopLevelSlashCommandData] must be used **once per top-level name**,
 * e.g., if you have `/tag create` and `/tag edit`, you can annotate at most one of them.
 *
 * Additionally, the first parameter must be:
 * - [GuildSlashEvent] if the [interaction contexts][TopLevelSlashCommandData.contexts]
 * only contains [InteractionContextType.GUILD].
 * - [GlobalSlashEvent] in other cases.
 *
 * ### Option types
 * - Input options: Uses [@SlashOption][SlashOption], varargs are supported with [@VarArgs][VarArgs],
 *   a list of mentions is also supported with [@MentionsString][MentionsString].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Generated options: Use [@GeneratedOption][GeneratedOption], implement [ApplicationGeneratedValueSupplierProvider]
 *   and return, on the correct command path/option name,
 *   an appropriate supplier that will generate an object of the correct type.
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * ### Permissions
 *
 * Required user/bot permissions can be set with [@UserPermissions][UserPermissions]/[@BotPermissions][BotPermissions].
 *
 * ### Declaration filtering
 *
 * You can prevent registration of **guild** commands with [@DeclarationFilter][DeclarationFilter].
 *
 * ### Execution filtering
 *
 * You can arbitrarily prevent execution of commands by using [@Filter][Filter],
 * passing an implementation of [ApplicationCommandFilter].
 *
 * ### Rate limiting
 *
 * See [@RateLimit][RateLimit] / [@Cooldown][Cooldown].
 *
 * @see GlobalApplicationCommandProvider Declaring global application commands using the DSL
 * @see GuildApplicationCommandProvider Declaring guild application commands using the DSL
 * @see AbstractApplicationCommandManager.slashCommand DSL equivalent
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDASlashCommand(
    /**
     * The top-level name of the command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     */
    val name: String,

    /**
     * Command group of this command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.subcommandGroup DSL equivalent
     */
    val group: String = "",

    /**
     * Subcommand name of this command, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see TopLevelSlashCommandBuilder.subcommand DSL equivalent (top-level command)
     * @see SlashSubcommandGroupBuilder.subcommand DSL equivalent (subcommand)
     */
    val subcommand: String = "",

    /**
     * Short description of the command displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped, example: `ban.description`.
     *
     * **Note:** A description cannot be set here and on [@TopLevelSlashCommandData][TopLevelSlashCommandData] at the same time.
     *
     * @see LocalizationFunction
     *
     * @see SlashCommandBuilder.description DSL equivalent
     */
    val description: String = ""
)
