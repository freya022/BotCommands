package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.components.ComponentDescriptor
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal class EnumResolver<E : Enum<E>> internal constructor(
    e: KClass<E>,
    private val values: Array<out E>,
    private val nameFunction: (e: E) -> String
) :
    ParameterResolver<EnumResolver<E>, E>(e),
    RegexParameterResolver<EnumResolver<E>, E>,
    SlashParameterResolver<EnumResolver<E>, E>,
    ComponentParameterResolver<EnumResolver<E>, E> {

    //region Regex
    //TODO test
    override val pattern: Pattern = Pattern.compile("(?i)(${values.joinToString("|") { Pattern.quote(nameFunction(it)) }})(?-i)")

    override val testExample: String = values.first().name

    override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return nameFunction(values.first())
    }

    override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): E = values.first { it.name.contentEquals(args[0], ignoreCase = true) }
    //endregion

    //region Slash
    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return values.map { Choice(nameFunction(it), it.name) }
    }

    override suspend fun resolveSuspend(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): E = values.first { it.name == optionMapping.asString }
    //endregion

    //region Component
    //TODO test
    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): E = values.first { it.name == arg }
    //endregion
}

//TODO docs
// do not forget predefined choices
inline fun <reified E : Enum<E>> enumResolver(vararg values: E = enumValues(), noinline nameFunction: (e: E) -> String): ParameterResolver<*, *> {
    return enumResolver(E::class, values, nameFunction)
}

@PublishedApi
internal fun <E : Enum<E>> enumResolver(e: KClass<E>, values: Array<out E>, nameFunction: (e: E) -> String): EnumResolver<E> {
    return EnumResolver(e, values, nameFunction)
}