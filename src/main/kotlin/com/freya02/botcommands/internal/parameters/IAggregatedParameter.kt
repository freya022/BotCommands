package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.Option
import kotlin.reflect.KFunction

interface IAggregatedParameter : MethodParameter {
    val aggregator: KFunction<*>
    /**
     * Nullable to accommodate for constructor aggregators (they are not tied to classes)
     */
    val aggregatorInstance: Any?

    //TODO rename to "options", not all options are command inputs
    val commandOptions: List<Option>
}