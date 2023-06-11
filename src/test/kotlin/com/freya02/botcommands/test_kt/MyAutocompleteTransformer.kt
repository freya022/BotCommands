package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import net.dv8tion.jda.api.interactions.commands.Command

@BService
@ServiceType(AutocompleteTransformer::class)
class MyAutocompleteTransformer : AutocompleteTransformer<CustomObject> {
    override fun getElementType() = CustomObject::class.java

    override fun apply(e: CustomObject) = Command.Choice(e.toString(), e.toString())
}