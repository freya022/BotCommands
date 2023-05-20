package com.freya02.botcommands.api.commands.builder

import kotlin.reflect.KFunction

interface IBuilderFunctionHolder<R> {
    val function: KFunction<R>
}