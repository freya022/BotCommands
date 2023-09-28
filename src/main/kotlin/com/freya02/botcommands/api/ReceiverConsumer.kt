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

    companion object {
        private val NOOP = ReceiverConsumer<Any?> { }

        @Suppress("UNCHECKED_CAST")
        fun <T> noop(): ReceiverConsumer<T> = NOOP as ReceiverConsumer<T>
    }
}
