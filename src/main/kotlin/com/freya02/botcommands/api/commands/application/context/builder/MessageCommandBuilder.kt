package com.freya02.botcommands.api.commands.application.context.builder

import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixin
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

class MessageCommandBuilder internal constructor(
    private val context: BContextImpl,
    name: String,
    function: KFunction<Any>,
    scope: CommandScope
) : ApplicationCommandBuilder(name), ITopLevelApplicationCommandBuilder by TopLevelApplicationCommandBuilderMixin(scope) {
    override val function = function.reflectReference()

    override val topLevelBuilder: ITopLevelApplicationCommandBuilder = this
    override val parentInstance: INamedCommand? = null

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun option(declaredName: String) {
        commandOptionBuilders[declaredName] = MessageCommandOptionBuilder(function, declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun customOption(declaredName: String) {
        commandOptionBuilders[declaredName] = CustomOptionBuilder(function, declaredName)
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        commandOptionBuilders[declaredName] = ApplicationGeneratedOptionBuilder(function, declaredName, generatedValueSupplier)
    }

    internal fun build(): MessageCommandInfo {
        return MessageCommandInfo(context, this)
    }
}
