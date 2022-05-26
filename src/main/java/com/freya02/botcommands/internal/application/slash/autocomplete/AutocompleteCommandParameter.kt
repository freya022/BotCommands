package com.freya02.botcommands.internal.application.slash.autocomplete

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.CompositeKey
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.internal.application.slash.AbstractSlashCommandParameter
import com.freya02.botcommands.internal.isSubclassOfAny
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

class AutocompleteCommandParameter(
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder //TODO Not sure about that
) : AbstractSlashCommandParameter(
    parameter, optionBuilder
) {
    val isCompositeKey = parameter.hasAnnotation<CompositeKey>()

    init {
        if (type.jvmErasure.isSubclassOfAny(User::class, Member::class, Channel::class, Role::class)) {
            throw IllegalArgumentException("Autocomplete parameters cannot have an entity (User/Member/Channel/Role) as a value")
        }
    }
}