package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.parameters.*
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal sealed class AbstractUserSnowflakeResolver<T : AbstractUserSnowflakeResolver<T, R>, R : UserSnowflake>(
    clazz: KClass<R>
) : ParameterResolver<T, R>(clazz),
    RegexParameterResolver<T, R>,
    SlashParameterResolver<T, R>,
    ComponentParameterResolver<T, R>,
    UserContextParameterResolver<T, R> {

    final override val pattern: Pattern = Pattern.compile("(?:<@!?)?(\\d+)>?")
    final override val testExample: String = "<@1234>"

    final override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return event.member.asMention
    }

    final override val optionType: OptionType = OptionType.USER
}