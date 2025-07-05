package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal class MessageContextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDAMessageCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDAMessageCommand>(classPathFunction, annotation, path, commandId)