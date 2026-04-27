package io.github.freya022.botcommands.internal.commands.builder

import kotlin.reflect.KFunction

interface IBuilderFunctionHolder<R> {
    val function: KFunction<R>
}
