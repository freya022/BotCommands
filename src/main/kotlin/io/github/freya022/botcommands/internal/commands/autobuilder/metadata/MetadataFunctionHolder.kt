package io.github.freya022.botcommands.internal.commands.autobuilder.metadata

import kotlin.reflect.KFunction

internal interface MetadataFunctionHolder {
    val func: KFunction<*>
}