package io.github.freya022.botcommands.internal.core.restarter

interface RestartClassLoaderAdapterFactory {

    fun wrapOrNull(loader: ClassLoader): RestartClassLoaderAdapter?
}
