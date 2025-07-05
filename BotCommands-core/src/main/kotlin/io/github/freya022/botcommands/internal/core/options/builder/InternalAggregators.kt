package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

@BService //Enables reflection metadata
internal object InternalAggregators {
    internal val theSingleAggregator = InternalAggregators::singleAggregator.reflectReference()
    internal val theVarargAggregator = InternalAggregators::varargAggregator.reflectReference()

    internal fun KFunction<*>.isSingleAggregator() = this === theSingleAggregator
    internal fun KFunction<*>.isVarargAggregator() = this === theVarargAggregator
    internal fun KFunction<*>.isSpecialAggregator() = isSingleAggregator() || isVarargAggregator()

    //The types should not matter as the checks are made against the command function
    @Suppress("MemberVisibilityCanBePrivate")
    internal fun singleAggregator(it: Any) = it

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun varargAggregator(args: List<Any>) = args
}