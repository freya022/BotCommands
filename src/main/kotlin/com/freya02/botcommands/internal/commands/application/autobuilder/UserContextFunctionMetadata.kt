package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import kotlin.reflect.KFunction

internal class UserContextFunctionMetadata(
    instance: ApplicationCommand,
    func: KFunction<*>,
    annotation: JDAUserCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<ApplicationCommand, JDAUserCommand>(instance, func, annotation, path, commandId)