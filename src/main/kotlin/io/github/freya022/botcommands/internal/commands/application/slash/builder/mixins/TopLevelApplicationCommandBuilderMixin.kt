package io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

internal interface TopLevelApplicationCommandBuilderMixin {
    // What TopLevelApplicationCommandBuilder has but without superinterfaces, to let us use delegates
    var contexts: Set<InteractionContextType>
    var integrationTypes: Set<IntegrationType>
    var isDefaultLocked: Boolean
    var nsfw: Boolean
}