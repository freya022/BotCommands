package io.github.freya022.botcommands.internal.core.restarter

import org.springframework.boot.devtools.restart.classloader.RestartClassLoader

private val targetClass: Class<*>? = try {
    Class.forName("org.springframework.boot.devtools.restart.classloader.RestartClassLoader")
} catch (_: ClassNotFoundException) {
    null
}

internal class SpringRestartClassLoaderAdapterFactory : RestartClassLoaderAdapterFactory {

    override fun wrapOrNull(loader: ClassLoader): RestartClassLoaderAdapter? {
        if (targetClass == null) return null

        return if (targetClass.isInstance(loader)) {
            SpringRestartClassLoaderAdapter(loader)
        } else {
            null
        }
    }

    private class SpringRestartClassLoaderAdapter(loader: ClassLoader) : RestartClassLoaderAdapter {

        private val loader = loader as RestartClassLoader

        override fun publicDefineClass(name: String, bytes: ByteArray): Class<*> =
            loader.publicDefineClass(name, bytes, null)
    }
}
