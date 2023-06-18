package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.AbstractSlashCommandOption
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

class AutocompleteCommandOption(
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {
    init {
        if (optionBuilder.parameter.type.jvmErasure.isSubclassOf(IMentionable::class)) {
            throw IllegalArgumentException("Autocomplete parameters cannot have an entity (anything extending IMentionable) as a value")
        }
    }
}