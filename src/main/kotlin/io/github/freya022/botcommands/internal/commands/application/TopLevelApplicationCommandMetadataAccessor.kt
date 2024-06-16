package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata

internal interface TopLevelApplicationCommandMetadataAccessor {
    var metadata: TopLevelApplicationCommandMetadata
}