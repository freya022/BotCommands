package com.freya02.botcommands.internal.application.slash.autocomplete

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CompositeKey
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.application.slash.AbstractSlashCommandParameter
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

class AutocompleteCommandParameter(
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder, //TODO Not sure about that
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandParameter(
    parameter, optionBuilder, resolver
) {
    val isCompositeKey = parameter.hasAnnotation<CompositeKey>()

    init {
        if (type.jvmErasure.isSubclassOf(IMentionable::class)) {
            throw IllegalArgumentException("Autocomplete parameters cannot have an entity (anything extending IMentionable) as a value")
        }
    }
}