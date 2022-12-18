package com.freya02.botcommands.internal.utils

object Benchmark {
    inline fun <R> printTimings(desc: String, block: () -> R): R {
        val start = System.nanoTime()
        return block().also {
            val end = System.nanoTime()
            println("%s took %.3f ms".format(desc, (end - start) / 1000000.0))
        }
    }
}