package com.freya02.botcommands.internal.core

internal class PreboundFunction(val classPathFunction: ClassPathFunction, val parametersBlock: () -> Array<Any>) {
    val parameters: Array<Any> by lazy {
        parametersBlock()
    }
}