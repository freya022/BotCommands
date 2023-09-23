package com.freya02.botcommands.api

fun interface ReceiverConsumer<T> : (T) -> Unit {
    fun T.accept()

    override fun invoke(p1: T) {
        with(p1) { accept() }
    }

    fun applyTo(receiver: T): T {
        with(this) { receiver.accept() }

        return receiver
    }
}

//TODO replace with Kotlin's apply
@Deprecated("Replaced with Kotlin's apply")
@Suppress("NOTHING_TO_INLINE")
inline fun <T, R : T> R.apply(block: ReceiverConsumer<T>): R {
    block.applyTo(this)
    return this
}