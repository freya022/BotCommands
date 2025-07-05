package io.github.freya022.botcommands.internal.commands.builder

import kotlin.reflect.KFunction

internal interface IBuilderFunctionHolder<R> {
    val function: KFunction<R>
}