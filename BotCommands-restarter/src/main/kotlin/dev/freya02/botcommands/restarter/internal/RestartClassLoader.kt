package dev.freya02.botcommands.restarter.internal

import java.net.URL
import java.net.URLClassLoader
import java.util.*

/**
 * This implementation is slightly simplified, in particular to avoid tracking (and keeping in-memory) the actual data of build outputs.
 *
 * However, code that loads classes that were deleted, before the restart (like shutdown code), will fail.
 *
 * But this issue should be extremely rare, so this is a fair trade.
 */
internal class RestartClassLoader internal constructor(
    urls: List<URL>,
    parent: ClassLoader,
) : URLClassLoader(urls.toTypedArray(), parent) {

    override fun getResources(name: String): Enumeration<URL> {
        return this.parent.getResources(name)
    }

    override fun getResource(name: String): URL? {
        return findResource(name) ?: super.getResource(name)
    }

    override fun findResource(name: String): URL? {
        return super.findResource(name)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return synchronized(getClassLoadingLock(name)) {
            val loadedClass = findLoadedClass(name) ?: try {
                findClass(name)
            } catch (_: ClassNotFoundException) {
                Class.forName(name, false, parent)
            }
            if (resolve) resolveClass(loadedClass)
            loadedClass
        }
    }

    override fun findClass(name: String): Class<*> {
        return super.findClass(name)
    }

    internal fun publicDefineClass(name: String, bytes: ByteArray): Class<*> {
        return defineClass(name, bytes, 0, bytes.size)
    }
}
