package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.isSubclassOfAny
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.function.Consumer
import kotlin.math.max
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

class SlashCommandInfo(
    context: BContext,
    builder: SlashCommandBuilder
) : ApplicationCommandInfo(
    context,
    builder
) {
    val description = builder.description

    /**
     * This is NOT localized
     */
    private val commandParameters: MethodParameters<SlashCommandParameter>

    init {
        commandParameters = MethodParameters.of(commandMethod) { i, parameter ->
            val type = parameter.type.jvmErasure
            if (type.isSubclassOfAny(Member::class, Role::class, GuildChannel::class)) {
                if (!isGuildOnly) {
                    throwUser("The slash command cannot have a " + type.simpleName + " parameter as it is not guild-only")
                }
            }
            SlashCommandParameter(parameter, i)
        }
    }

    @Throws(Exception::class)
    fun execute(
        context: BContextImpl,
        event: SlashCommandInteractionEvent,
        throwableConsumer: Consumer<Throwable>
    ): Boolean {
        val objects: MutableList<Any?> = ArrayList(commandParameters.size + 1)
        objects += if (isGuildOnly) GuildSlashEvent(context, commandMethod, event) else GlobalSlashEventImpl(context, commandMethod, event)

        for (parameter in commandParameters) {
            val guild = event.guild
            if (guild != null) {
                val supplier = parameter.defaultOptionSupplierMap[guild.idLong]
                if (supplier != null) {
                    val defaultVal = supplier.getDefaultValue(event)
                    SlashUtils.checkDefaultValue(this, parameter, defaultVal)
                    objects.add(defaultVal)
                    continue
                }
            }
            val arguments = max(1, parameter.varArgs)
            val objectList: MutableList<Any?> = ArrayList(arguments)
            val applicationOptionData = parameter.applicationOptionData
            if (parameter.isOption) {
                val optionName = applicationOptionData.effectiveName
                for (varArgNum in 0 until arguments) {
                    val varArgName = SlashUtils.getVarArgName(optionName, varArgNum)
                    val optionMapping = event.getOption(varArgName)
                        ?: if (parameter.isOptional || parameter.isVarArg && !parameter.isRequiredVararg(varArgNum)) {
                            objectList += when {
                                parameter.isPrimitive -> 0
                                else -> null
                            }

                            continue
                        } else {
                            throwUser("Slash parameter couldn't be resolved at parameter " + applicationOptionData.effectiveName + " (" + varArgName + ")")
                        }

                    val resolved = parameter.resolver.resolve(context, this, event, optionMapping)
                    if (resolved == null) {
                        event.reply(
                            context.getDefaultMessages(event).getSlashCommandUnresolvableParameterMsg(
                                applicationOptionData.effectiveName,
                                parameter.boxedType.jvmErasure.simpleName
                            )
                        )
                            .setEphemeral(true)
                            .queue()

                        //Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
                        LOGGER.trace(
                            "The parameter '{}' of value '{}' could not be resolved into a {}",
                            applicationOptionData.effectiveName,
                            optionMapping.asString,
                            parameter.boxedType.jvmErasure.simpleName
                        )

                        return false
                    }

                    require(parameter.boxedType.jvmErasure.isSuperclassOf(resolved::class)) {
                        "The parameter '%s' of value '%s' is not a valid type (expected a %s)".format(
                            applicationOptionData.effectiveName,
                            optionMapping.asString,
                            parameter.boxedType.jvmErasure.simpleName
                        )
                    }

                    objectList.add(resolved)
                }
            } else {
                objectList.add(parameter.customResolver.resolve(context, this, event))
            }

            //For some reason using an array list instead of a regular array
            // magically unboxes primitives when passed to Method#invoke
            objects.add(if (parameter.isVarArg) objectList else objectList[0])
        }

        applyCooldown(event)

        try {
            commandMethod.call(*objects.toTypedArray())
        } catch (e: Throwable) {
            throwableConsumer.accept(e)
        }

        return true
    }

    fun getAutocompletionHandlerName(event: CommandAutoCompleteInteractionEvent): String? {
        val autoCompleteQuery = event.focusedOption
        for (parameter in commandParameters) {
            val applicationOptionData = parameter.applicationOptionData
            if (parameter.isOption) {
                val optionName = applicationOptionData.effectiveName
                if (optionName == autoCompleteQuery.name) {
                    return applicationOptionData.autocompletionHandlerName
                }
            }
        }
        return null
    }

    override fun getParameters(): MethodParameters<SlashCommandParameter> {
        return commandParameters
    }

    @Suppress("UNCHECKED_CAST")
    override fun getOptionParameters(): List<SlashCommandParameter> {
        return super.getOptionParameters() as List<SlashCommandParameter>
    }

    companion object {
        private val LOGGER = Logging.getLogger()
    }
}