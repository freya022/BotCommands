package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumNameFunction
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

internal class TextCommandEnumResolverImpl<E : Enum<E>> internal constructor(
    enumType: Class<E>,
    values: Set<E>,
    nameFunction: EnumNameFunction<E>,
    ignoreCase: Boolean,
) : ClassParameterResolver<TextCommandEnumResolverImpl<E>, E>(enumType),
    TextParameterResolver<TextCommandEnumResolverImpl<E>, E> {

    // Key is both the enum name and the human name
    private val enumMap: Map<String, E> = buildMap {
        enumType.enumConstants.forEach {
            this[it.name.lowercase()] = it
            this[nameFunction.apply(it).lowercase()] = it
        }
    }

    override val pattern: Pattern = Pattern.compile(
        "(${values.joinToString("|") { Pattern.quote(nameFunction.apply(it)) }})",
        if (ignoreCase) Pattern.CASE_INSENSITIVE else 0
    )

    override val testExample: String = nameFunction.apply(values.first())

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = testExample

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): E? = getEnumValueOrNull(args[0]!!)

    private fun getEnumValueOrNull(name: String): E? = enumMap[name.lowercase()]
}
