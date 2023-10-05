package io.github.freya022.botcommands.api.localization.arguments.factories

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.arguments.FormattableArgument
import io.github.freya022.botcommands.api.localization.arguments.MessageFormatArgument
import java.util.*

@BService
class MessageFormatArgumentFactory : FormattableArgumentFactory {
    override val regex: Regex = Regex("""(\w+)(,?.*?)""")

    override fun get(matchResult: MatchResult, locale: Locale): FormattableArgument {
        val (formatterName, formatterFormat) = matchResult.destructured
        //Replace named index by integer index
        val messageFormatter = "{0$formatterFormat}"
        return MessageFormatArgument(formatterName, messageFormatter, locale)
    }
}