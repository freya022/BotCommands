package io.github.freya022.botcommands.internal.utils

import net.dv8tion.jda.api.interactions.commands.CommandInteraction

internal val CommandInteraction.uniqueCommandPath: String
    get() = buildString(/* 19 + 8 (average length) */ 27) {
        append(commandIdLong)
        if (subcommandGroup != null)
            append(' ').append(subcommandGroup)
        if (subcommandName != null)
            append(' ').append(subcommandName)
    }
