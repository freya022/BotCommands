package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import net.dv8tion.jda.api.entities.Guild

/**
 * Provider of [ApplicationGeneratedValueSupplier] for [generated options][GeneratedOption] of **annotated** application commands.
 *
 * When using generated options, you must implement this on the same declaring class.
 */
interface ApplicationGeneratedValueSupplierProvider {
    /**
     * Returns the generated value supplier of a [@GeneratedOption][GeneratedOption].
     *
     * This function will only be called once per command option per guild.
     *
     * @param guild         The [Guild] in which to add the default value, `null` if the scope is **not** [CommandScope.GUILD]
     * @param commandId     The ID of the command, as optionally set in [@CommandId][CommandId], might be `null`
     * @param commandPath   The path of the application command
     * @param optionName    The option name, not the same as the parameter name, this is the same name that appears on Discord
     * @param parameterType The **boxed** type of the command option
     *
     * @return A [ApplicationGeneratedValueSupplier] to generate the option on command execution
     */
    fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?, commandPath: CommandPath,
        optionName: String, parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier
}
