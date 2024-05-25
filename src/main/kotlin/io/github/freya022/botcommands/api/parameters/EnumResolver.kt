package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

internal class EnumResolver<E : Enum<E>> internal constructor(
    e: Class<E>,
    private val guildValuesSupplier: EnumValuesSupplier<E>,
    private val nameFunction: EnumNameFunction<E>
) : ClassParameterResolver<EnumResolver<E>, E>(e),
    SlashParameterResolver<EnumResolver<E>, E>,
    ComponentParameterResolver<EnumResolver<E>, E>,
    TimeoutParameterResolver<EnumResolver<E>, E> {

    // Key is both the enum name and the human name
    private val enumMap: Map<String, E> = buildMap {
        e.enumConstants.forEach {
            this[it.name.lowercase()] = it
            this[nameFunction.apply(it).lowercase()] = it
        }
    }

    //region Slash
    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Command.Choice> {
        return guildValuesSupplier.get(guild).map { Command.Choice(nameFunction.apply(it), it.name) }
    }

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): E = getEnumValue(optionMapping.asString)
    //endregion

    //region Component
    override suspend fun resolveSuspend(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): E? = getEnumValueOrNull(arg)
    //endregion

    //region Timeout
    override suspend fun resolveSuspend(arg: String): E = getEnumValue(arg)
    //endregion

    override fun toString(): String {
        return "EnumResolver(guildValuesSupplier=$guildValuesSupplier, nameFunction=$nameFunction)"
    }

    private fun getEnumValue(name: String): E = getEnumValueOrNull(name) ?: throwInternal("Could not find enum value '$name', map: $enumMap")
    private fun getEnumValueOrNull(name: String): E? = enumMap[name.lowercase()]
}

/**
 * Transforms an enum entry into a human-readable name.
 *
 * @see Resolvers.enumResolver
 */
fun interface EnumNameFunction<E : Enum<E>> {
    fun apply(value: E): String
}

/**
 * Retrieves the enum entries to be pushed to the provided scope.
 *
 * @see enumResolver
 */
fun interface EnumValuesSupplier<E : Enum<E>> {
    /**
     * @param guild The guild containing the command, `null` for global commands
     */
    fun get(guild: Guild?): Collection<E>
}