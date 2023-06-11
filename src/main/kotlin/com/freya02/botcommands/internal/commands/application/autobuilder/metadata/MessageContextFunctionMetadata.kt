package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.internal.core.ClassPathFunction

internal class MessageContextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDAMessageCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDAMessageCommand>(classPathFunction, annotation, path, commandId)