package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.annotations.MentionsString
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.AbstractSlashCommandOption
import io.github.freya022.botcommands.internal.utils.classRef
import net.dv8tion.jda.api.entities.IMentionable
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

class AutocompleteCommandOption(
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {
    init {
        val kType = optionBuilder.parameter.type
        if (kType.jvmErasure.isSubclassOf(IMentionable::class)) {
            throw IllegalArgumentException("Autocomplete parameters cannot have entities extending ${classRef<IMentionable>()}")
        } else if (optionBuilder.parameter.hasAnnotation<MentionsString>()) {
            throw IllegalArgumentException("Autocomplete parameters cannot have strings of mentions")
        }
    }
}