package com.freya02.botcommands.internal.core

import kotlin.time.Duration

internal class EventHandlerFunction(val classPathFunction: ClassPathFunction,
                                    val isAsync: Boolean,
                                    val timeout: Duration,
                                    private val parametersBlock: () -> Array<Any>) {
    val parameters: Array<Any> by lazy {
        parametersBlock()
    }
}
