package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.internal.core.ClassPathFunction

internal class UserContextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDAUserCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDAUserCommand>(classPathFunction, annotation, path, commandId)