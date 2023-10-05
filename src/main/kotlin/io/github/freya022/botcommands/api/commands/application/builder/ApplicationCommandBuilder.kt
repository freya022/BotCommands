package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.internal.core.BContextImpl
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    context: BContextImpl,
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilder<T, Any>(context, name, function) {
    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    /**
     * Specifies whether the application command is usable in NSFW channels.<br>
     * Note: NSFW commands need to be enabled by the user in order to appear in DMs
     *
     * **Default:** false
     *
     * See the [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007) for more details.
     *
     * @return `true` if the command is restricted to NSFW channels
     *
     * @see JDASlashCommand.nsfw
     * @see JDAUserCommand.nsfw
     * @see JDAMessageCommand.nsfw
     */
    var nsfw: Boolean = false

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }
}
