package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import kotlin.reflect.KFunction

internal class MessageContextFunctionMetadata(
    instance: ApplicationCommand,
    func: KFunction<*>,
    annotation: JDAMessageCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<ApplicationCommand, JDAMessageCommand>(instance, func, annotation, path, commandId)