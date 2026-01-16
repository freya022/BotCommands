package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command

/**
 * Base class for **annotated** application commands such as slash / context commands.
 *
 * You are not required to use this if you use the application command provider interfaces.
 *
 * @see JDASlashCommand @JDASlashCommand
 * @see JDAMessageCommand @JDAMessageCommand
 * @see JDAUserCommand @JDAUserCommand
 *
 * @see SlashOptionChoiceProvider
 */
abstract class ApplicationCommand : SlashOptionChoiceProvider {
    override fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Command.Choice> {
        return emptyList()
    }

    /**
     * Returns the generated value supplier of a [@GeneratedOption][GeneratedOption].
     *
     * This function will only be called once per command option per guild.
     *
     * @param guild         The [Guild] in which to add the default value, `null` if the scope is **not** [CommandScope.GUILD]
     * @param commandId     The ID of the command, as optionally set in [@CommandId][CommandId], might be `null`
     * @param commandPath   The path of the command, as set in [@JDASlashCommand][JDASlashCommand]
     * @param optionName    The option name, not the same as the parameter name, this is the same name that appears on Discord
     * @param parameterType The **boxed** type of the command option
     *
     * @return A [ApplicationGeneratedValueSupplier] to generate the option on command execution
     */
    open fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?, commandPath: CommandPath,
        optionName: String, parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        val errorStr = buildString {
            append("Option '$optionName' in command path '${commandPath.fullPath}'")
            if (commandId != null) append(" (id '$commandId')")
            if (guild != null) append(" in guild '${guild.name}' (id ${guild.id})")
            append(" is a generated option but no generated value supplier has been given")
        }

        throwArgument(errorStr)
    }
}
