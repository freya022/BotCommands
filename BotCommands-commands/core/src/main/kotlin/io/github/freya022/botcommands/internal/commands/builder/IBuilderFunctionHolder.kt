package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import kotlin.reflect.KFunction

interface IBuilderFunctionHolder<R> {
    val function: KFunction<R>
}

inline fun <reified T : Any, R> IBuilderFunctionHolder<R>.toMemberParamFunction(context: BContext): MemberParamFunction<T, R> {
    return MemberParamFunction(context, this.function, T::class)
}
