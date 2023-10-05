package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal class UserContextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDAUserCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDAUserCommand>(classPathFunction, annotation, path, commandId)