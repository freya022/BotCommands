package com.freya02.botcommands.internal.commands.application.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import kotlin.reflect.KFunction

internal class SlashFunctionMetadata(
    instance: ApplicationCommand,
    func: KFunction<*>,
    annotation: JDASlashCommand,
    path: CommandPath,
    commandId: String?
) : ApplicationFunctionMetadata<ApplicationCommand, JDASlashCommand>(instance, func, annotation, path, commandId)