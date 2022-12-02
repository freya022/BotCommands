package com.freya02.botcommands.api

fun interface ReceiverConsumer<T> {
    fun T.accept()

    fun applyTo(receiver: T): T {
        with(this) { receiver.accept() }

        return receiver
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, R : T> R.apply(block: ReceiverConsumer<T>): R {
    block.applyTo(this)
    return this
}