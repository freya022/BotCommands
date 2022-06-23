package com.freya02.botcommands.api

fun interface ReceiverConsumer<T> {
    fun T.accept()

    fun applyTo(receiver: T) {
        with(this) { receiver.accept() }
    }
}