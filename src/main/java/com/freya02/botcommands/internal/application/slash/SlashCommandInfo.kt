package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.application.builder.findOption
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashUtils2.checkDefaultValue
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.isJava
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.function.Consumer
import kotlin.math.max
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class SlashCommandInfo internal constructor(
    context: BContextImpl,
    builder: SlashCommandBuilder
) : ApplicationCommandInfo(
    context,
    builder
) {
    val description = builder.description

    override val parameters: MethodParameters

    @Suppress("UNCHECKED_CAST")
    override val optionParameters: List<MethodParameter>
        get() = super.optionParameters as List<SlashCommandParameter>

    init {
        parameters = MethodParameters.of<SlashParameterResolver>(method, builder.optionBuilders) { kParameter, paramName, resolver ->
            val type = kParameter.type.jvmErasure
            if (type.isSubclassOfAny(Member::class, Role::class, GuildChannel::class)) {
                requireUser(isGuildOnly) {
                    "The slash command cannot have a ${type.simpleName} parameter as it is not guild-only"
                }
            }

            val optionBuilder = builder.optionBuilders.findOption<SlashCommandOptionBuilder>(paramName)
            SlashCommandParameter(kParameter, optionBuilder, resolver)
        }
    }

    @Throws(Exception::class)
    fun execute(
        context: BContextImpl,
        event: SlashCommandInteractionEvent,
        throwableConsumer: Consumer<Throwable>
    ): Boolean {
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.valueParameters.first()] =
            if (isGuildOnly) GuildSlashEvent(context, method, event) else GlobalSlashEventImpl(context, method, event)

        for (parameter in parameters) {
            if (parameter.methodParameterType == MethodParameterType.COMMAND) {
                parameter as SlashCommandParameter

                val guild = event.guild
                if (guild != null) {
                    val supplier = parameter.defaultOptionSupplierMap[guild.idLong]
                    if (supplier != null) {
                        val defaultVal = supplier.getDefaultValue(event)
                        checkDefaultValue(parameter, defaultVal)

                        objects[parameter.kParameter] = defaultVal

                        continue
                    }
                }

                val arguments = max(1, parameter.varArgs)
                val objectList: MutableList<Any?> = ArrayList(arguments)

                val optionName = parameter.discordName
                for (varArgNum in 0 until arguments) {
                    val varArgName = SlashUtils.getVarArgName(optionName, varArgNum) //TODO extension or infix
                    val optionMapping = event.getOption(varArgName)
                        ?: if (parameter.isOptional || (parameter.isVarArg && !parameter.isRequiredVararg(varArgNum))) {
                            objectList += when {
                                parameter.isPrimitive -> when {
                                    parameter.kParameter.isJava -> 0
                                    else -> null
                                }
                                else -> null
                            }

                            continue
                        } else {
                            throwUser("Slash parameter couldn't be resolved at parameter " + parameter.name + " (" + varArgName + ")")
                        }

                    val resolved = parameter.resolver.resolve(context, this, event, optionMapping)
                    if (resolved == null) {
                        event.reply(
                            context.getDefaultMessages(event).getSlashCommandUnresolvableParameterMsg(
                                parameter.name,
                                parameter.type.jvmErasure.simpleName
                            )
                        )
                            .setEphemeral(true)
                            .queue()

                        //Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
                        LOGGER.trace(
                            "The parameter '{}' of value '{}' could not be resolved into a {}",
                            parameter.name,
                            optionMapping.asString,
                            parameter.type.jvmErasure.simpleName
                        )

                        return false
                    }

                    requireUser(parameter.type.jvmErasure.isSuperclassOf(resolved::class)) {
                        "The parameter '%s' of value '%s' is not a valid type (expected a %s)".format(
                            parameter.name,
                            optionMapping.asString,
                            parameter.type.jvmErasure.simpleName
                        )
                    }

                    objectList.add(resolved)
                }

                objects[parameter.kParameter] = if (parameter.isVarArg) objectList else objectList[0]
            } else if (parameter.methodParameterType == MethodParameterType.CUSTOM) {
                parameter as CustomMethodParameter

                objects[parameter.kParameter] = parameter.resolver.resolve(context, this, event)
            } else {
                TODO()
            }
        }

        applyCooldown(event)

        try {
            method.callBy(objects)
        } catch (e: Throwable) {
            throwableConsumer.accept(e)
        }

        return true
    }

    fun getAutocompletionHandlerName(event: CommandAutoCompleteInteractionEvent): String? {
        return null //TODO autocomplete
//        val autoCompleteQuery = event.focusedOption
//        for (parameter in parameters) {
//            val applicationOptionData = parameter.applicationOptionData
//            if (parameter.methodParameterType == MethodParameterType.COMMAND) {
//                parameter as SlashCommandParameter
//
//                val optionName = parameter.name
//                if (optionName == autoCompleteQuery.name) {
//                    return parameter.auto
//                }
//            }
//        }
//        return null
    }

    companion object {
        private val LOGGER = Logging.getLogger()
    }
}