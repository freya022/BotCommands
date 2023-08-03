package com.freya02.botcommands.api.localization.arguments.factories

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.localization.arguments.FormattableArgument
import com.freya02.botcommands.api.localization.arguments.JavaFormattableArgument
import java.util.*

@BService
@ServiceType(FormattableArgumentFactory::class)
class JavaFormattableArgumentFactory : FormattableArgumentFactory {
    override val regex: Regex = Regex("""\{(\w+):(%.+?)}""")

    override fun get(matchResult: MatchResult, locale: Locale): FormattableArgument {
        val (formatterName, formatter) = matchResult.destructured
        return JavaFormattableArgument(formatterName, formatter, locale)
    }
}