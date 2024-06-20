package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Resolver
class TextTimeUnitResolver : ClassParameterResolver<TextTimeUnitResolver, TimeUnit>(TimeUnit::class),
                             TextParameterResolver<TextTimeUnitResolver, TimeUnit> {

    override val pattern: Pattern = Pattern.compile("(minutes|hours|days|weeks)")
    override val testExample: String = "days"

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "days"
}