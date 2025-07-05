package io.github.freya022.botcommands.api.localization.arguments.factories

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.arguments.FormattableArgument
import io.github.freya022.botcommands.api.localization.arguments.JavaFormattableArgument
import java.util.*

@BService
class JavaFormattableArgumentFactory : FormattableArgumentFactory {
    override val regex: Regex = Regex("""(\w+):(%.+?)""")

    override fun get(matchResult: MatchResult, locale: Locale): FormattableArgument {
        val (formatterName, formatter) = matchResult.destructured
        return JavaFormattableArgument(formatterName, formatter, locale)
    }
}