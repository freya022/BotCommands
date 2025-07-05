package io.github.freya022.botcommands.internal.utils

import gnu.trove.TIntCollection

internal inline fun TIntCollection.any(block: (Int) -> Boolean): Boolean {
    for (it in this)
        if (block(it))
            return true
    return false
}