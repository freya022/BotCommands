package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

internal class TextEnumResolver<E : Enum<E>> internal constructor(
    builder: EnumResolverBuilder<E>,
    textSupport: EnumResolverBuilder<E>.TextSupport
) : AbstractEnumResolver<TextEnumResolver<E>, E>(builder),
    TextParameterResolver<TextEnumResolver<E>, E> {

    override val pattern: Pattern = Pattern.compile(
        "(${textSupport.values.joinToString("|") { Pattern.quote(textSupport.nameFunction.apply(it)) }})",
        if (textSupport.ignoreCase) Pattern.CASE_INSENSITIVE else 0
    )

    override val testExample: String = textSupport.nameFunction.apply(textSupport.values.first())

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = testExample

    override suspend fun resolveSuspend(
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): E? = getEnumValueOrNull(args[0]!!)
}

internal class EnumResolver<E : Enum<E>> internal constructor(
    builder: EnumResolverBuilder<E>
) : AbstractEnumResolver<EnumResolver<E>, E>(builder)

internal sealed class AbstractEnumResolver<T : AbstractEnumResolver<T, E>, E : Enum<E>>(
    builder: EnumResolverBuilder<E>
) : ClassParameterResolver<T, E>(builder.enumType),
    SlashParameterResolver<T, E>,
    ComponentParameterResolver<T, E>,
    TimeoutParameterResolver<T, E> {

    private val guildValuesSupplier: EnumValuesSupplier<E> = builder.guildValuesSupplier
    private val nameFunction: EnumNameFunction<E> = builder.nameFunction

    // Key is both the enum name and the human name
    private val enumMap: Map<String, E> = buildMap {
        builder.enumType.enumConstants.forEach {
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
    override suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String): E? =
        getEnumValueOrNull(arg)
    //endregion

    //region Timeout
    override suspend fun resolveSuspend(arg: String): E = getEnumValue(arg)
    //endregion

    override fun toString(): String {
        return "EnumResolver(guildValuesSupplier=$guildValuesSupplier, nameFunction=$nameFunction)"
    }

    private fun getEnumValue(name: String): E = getEnumValueOrNull(name) ?: throwInternal("Could not find enum value '$name', map: $enumMap")
    protected fun getEnumValueOrNull(name: String): E? = enumMap[name.lowercase()]
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