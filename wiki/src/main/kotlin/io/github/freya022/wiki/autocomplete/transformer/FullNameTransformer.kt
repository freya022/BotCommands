package io.github.freya022.wiki.autocomplete.transformer

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.interactions.commands.Command

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:full_name_obj-kotlin]
data class FullName(val firstName: String, val secondName: String)
// --8<-- [end:full_name_obj-kotlin]

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:autocomplete_transformer-kotlin]
@BService
class FullNameTransformer : AutocompleteTransformer<FullName> {
    override fun getElementType(): Class<FullName> = FullName::class.java

    override fun apply(e: FullName): Command.Choice {
        return Command.Choice("${e.firstName} ${e.secondName}", "${e.firstName}|${e.secondName}")
    }
}
// --8<-- [end:autocomplete_transformer-kotlin]