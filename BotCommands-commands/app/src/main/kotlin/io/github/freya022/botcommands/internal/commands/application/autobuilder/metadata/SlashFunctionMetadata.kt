package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal class SlashFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDASlashCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDASlashCommand>(classPathFunction, annotation, path, commandId)