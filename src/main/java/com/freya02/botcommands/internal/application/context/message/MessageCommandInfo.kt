package com.freya02.botcommands.internal.application.context.message

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.context.ContextCommandParameter
import com.freya02.botcommands.internal.requireFirstParam
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import java.util.function.Consumer
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class MessageCommandInfo(context: BContext, builder: UserCommandBuilder) : ApplicationCommandInfo(context, builder) {
    private val commandParameters: MethodParameters<ContextCommandParameter<MessageContextParameterResolver>>

    init {
        requireFirstParam(method.valueParameters, GlobalMessageEvent::class)

        commandParameters = MethodParameters.of(method) { i: Int, parameter: KParameter ->
            ContextCommandParameter(MessageContextParameterResolver::class.java, parameter, i)
        }
    }

    @Throws(Exception::class)
    fun execute(
        context: BContextImpl,
        event: MessageContextInteractionEvent,
        throwableConsumer: Consumer<Throwable>
    ): Boolean {
        val objects: MutableList<Any?> = ArrayList(commandParameters.size + 1)
        objects +=
            if (isGuildOnly) GuildMessageEvent(method, context, event) else GlobalMessageEvent(method, context, event)

        for (i in 0 until commandParameters.size) {
            val parameter = commandParameters[i]
            if (parameter.isOption) {
                objects[i + 1] = parameter.resolver.resolve(context, this, event)

                //no need to check for unresolved parameters,
                // it is impossible to have other arg types other than Message (and custom resolvers)
            } else {
                objects[i + 1] = parameter.customResolver.resolve(context, this, event)
            }
        }

        applyCooldown(event)

        try {
            commandMethod.call(*objects.toTypedArray())
        } catch (e: Throwable) {
            throwableConsumer.accept(e)
        }

        return true
    }

    override fun getParameters(): MethodParameters<ContextCommandParameter<MessageContextParameterResolver>> {
        return commandParameters
    }
}