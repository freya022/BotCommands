package com.freya02.botcommands.api.commands.builder

import kotlin.reflect.KFunction

@Deprecated("To be removed")
open class BuilderFunctionHolder<R> internal constructor() : IBuilderFunctionHolder<R> {
    override val function: KFunction<R>
        get() = TODO("Not yet implemented")

}