package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import kotlin.reflect.KFunction

internal class UserContextFunctionMetadata(
    instanceSupplier: () -> ApplicationCommand,
    func: KFunction<*>,
    annotation: JDAUserCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<ApplicationCommand, JDAUserCommand>(instanceSupplier, func, annotation, path, commandId)