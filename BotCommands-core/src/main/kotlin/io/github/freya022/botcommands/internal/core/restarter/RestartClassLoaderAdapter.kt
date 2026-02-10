package io.github.freya022.botcommands.internal.core.restarter

import java.util.*

interface RestartClassLoaderAdapter {

    fun publicDefineClass(name: String, bytes: ByteArray): Class<*>

    companion object {

        private val factories: List<RestartClassLoaderAdapterFactory> =
            ServiceLoader
                .load(RestartClassLoaderAdapterFactory::class.java)
                .toList()

        fun wrapOrNull(loader: ClassLoader): RestartClassLoaderAdapter? {
            return factories.firstNotNullOfOrNull { it.wrapOrNull(loader) }
        }
    }
}
