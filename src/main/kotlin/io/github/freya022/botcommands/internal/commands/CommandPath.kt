package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandPath

val CommandPath.components: List<String>
    get() = fullPath.split(' ')

val CommandPath.spacedPath: String get() = getFullPath(' ')
