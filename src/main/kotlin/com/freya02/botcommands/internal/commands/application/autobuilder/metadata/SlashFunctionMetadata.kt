package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.internal.core.ClassPathFunction

internal class SlashFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDASlashCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDASlashCommand>(classPathFunction, annotation, path, commandId)