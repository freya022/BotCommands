package dev.freya02.botcommands.restarter.internal

import java.net.URL
import java.net.URLClassLoader
import java.util.*

// STILL SUPER DUPER IMPORTANT TO OVERRIDE SOME STUFF AND DELEGATE
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
}
