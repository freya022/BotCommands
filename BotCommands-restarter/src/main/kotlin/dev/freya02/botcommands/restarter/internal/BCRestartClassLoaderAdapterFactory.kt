package dev.freya02.botcommands.restarter.internal

import io.github.freya022.botcommands.internal.core.restarter.RestartClassLoaderAdapter
import io.github.freya022.botcommands.internal.core.restarter.RestartClassLoaderAdapterFactory

internal class BCRestartClassLoaderAdapterFactory : RestartClassLoaderAdapterFactory {

    override fun wrapOrNull(loader: ClassLoader): RestartClassLoaderAdapter? {
        return if (loader is RestartClassLoader) {
            BCRestartClassLoaderAdapter(loader)
        } else {
            null
        }
    }

    private class BCRestartClassLoaderAdapter(private val loader: RestartClassLoader) : RestartClassLoaderAdapter {

        override fun publicDefineClass(name: String, bytes: ByteArray): Class<*> =
            loader.publicDefineClass(name, bytes)
    }
}
