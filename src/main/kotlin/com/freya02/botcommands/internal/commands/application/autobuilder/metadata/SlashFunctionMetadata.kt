package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import kotlin.reflect.KFunction

internal class SlashFunctionMetadata(
    instanceSupplier: () -> ApplicationCommand,
    func: KFunction<*>,
    annotation: JDASlashCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<ApplicationCommand, JDASlashCommand>(instanceSupplier, func, annotation, path, commandId)