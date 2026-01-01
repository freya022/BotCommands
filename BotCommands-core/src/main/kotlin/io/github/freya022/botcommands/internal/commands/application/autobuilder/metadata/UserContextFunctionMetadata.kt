package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal class UserContextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDAUserCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<JDAUserCommand>(classPathFunction, annotation, path, commandId), RootAnnotatedApplicationCommand {
    override val metadata: ApplicationFunctionMetadata<*>
        get() = this
    override val scope: CommandScope
        get() = annotation.scope
    override val name: String
        get() = path.name
}
