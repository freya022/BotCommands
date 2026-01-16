package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
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
abstract class ApplicationCommand : SlashOptionChoiceProvider, ApplicationGeneratedValueSupplierProvider {
    override fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Command.Choice> {
        return emptyList()
    }

    override fun getGeneratedValueSupplier(
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
