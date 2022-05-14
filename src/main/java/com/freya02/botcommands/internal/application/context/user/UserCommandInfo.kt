package com.freya02.botcommands.internal.application.context.user

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.application.context.user.GuildUserEvent
import com.freya02.botcommands.api.parameters.UserContextParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.context.ContextCommandParameter
import com.freya02.botcommands.internal.requireFirstParam
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import java.util.function.Consumer
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class UserCommandInfo(context: BContext, builder: UserCommandBuilder) : ApplicationCommandInfo(context, builder) {
    override val parameters: MethodParameters<ContextCommandParameter<UserContextParameterResolver>>

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        parameters = MethodParameters.of(method) { i: Int, parameter: KParameter ->
            ContextCommandParameter(UserContextParameterResolver::class, parameter, i)
        }
    }

    @Throws(Exception::class)
    fun execute(
        context: BContextImpl,
        event: UserContextInteractionEvent,
        throwableConsumer: Consumer<Throwable>
    ): Boolean {
        val objects: MutableList<Any?> = ArrayList(parameters.size + 1)
        objects += if (isGuildOnly) GuildUserEvent(method, context, event) else GlobalUserEvent(
            method, context, event)

        parameters.forEachIndexed { i, parameter ->
            if (parameter.isOption) {
                objects[i + 1] = parameter.resolver.resolve(context, this, event)
                //no need to check for unresolved parameters,
                // it is impossible to have other arg types other than User (and custom resolvers)
            } else {
                objects[i + 1] = parameter.customResolver.resolve(context, this, event)
            }
        }

        applyCooldown(event)

        try {
            method.call(*objects.toTypedArray())
        } catch (e: Throwable) {
            throwableConsumer.accept(e)
        }

        return true
    }
}