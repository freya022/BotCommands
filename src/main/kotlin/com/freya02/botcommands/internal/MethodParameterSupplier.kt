package com.freya02.botcommands.internal

fun interface MethodParameterSupplier<T> {
    fun supply(): T //TODO not sure if method parameter suppliers should be constant or not
}