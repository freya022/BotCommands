package io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope

internal interface TopLevelApplicationCommandBuilderMixin {
    // What TopLevelApplicationCommandBuilder has but without superinterfaces, to let us use delegates
    val scope: CommandScope
    var isDefaultLocked: Boolean
    var nsfw: Boolean
}