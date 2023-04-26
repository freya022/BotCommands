package com.freya02.botcommands.api.commands.builder

import kotlin.reflect.KFunction

internal interface IBuilderFunctionHolder<R> {
    val function: KFunction<R>
}