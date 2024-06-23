package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.interactions.commands.Command

@BService
class MyAutocompleteTransformer : AutocompleteTransformer<CustomObject> {
    override val elementType: Class<CustomObject>
        get() = CustomObject::class.java

    override fun apply(e: CustomObject) = Command.Choice(e.toString(), e.toString())
}