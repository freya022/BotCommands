package io.github.freya022.botcommands.internal.modals.utils

import net.dv8tion.jda.api.components.Component.Type.*
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.ModalMapping

internal val ModalInteractionEvent.allValuesAsString: String
    get() = values.map { it.valueAsString }.toString()

internal val ModalMapping.valueAsString: String
    get() = when (type) {
        STRING_SELECT -> asStringList.toString()
        TEXT_INPUT -> asString
        CHANNEL_SELECT, ROLE_SELECT, USER_SELECT, MENTIONABLE_SELECT -> asLongList.toString()
        else -> toString()
    }
