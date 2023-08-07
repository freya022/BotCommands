package com.freya02.botcommands.api.core.utils

import com.freya02.botcommands.api.DefaultMessages
import mu.KLogger
import mu.KotlinLogging
import mu.toKLogger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.*

@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> KotlinLogging.logger(): KLogger =
    LoggerFactory.getLogger(T::class.java).toKLogger()

private val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

/**
 * Reads a resource relative from the calling class.
 *
 * If the URL starts with a `/`, then the resource will be read from the root,
 * so either the JAR root, or from your `resources` directory.
 *
 * If the URL does not start with a `/`, then it is read relative to the package of the calling class.
 */
fun readResource(url: String): InputStream {
    val callerClass = stackWalker.callerClass
    return requireNotNull(callerClass.getResourceAsStream(url)) {
        "Resource of class " + callerClass.simpleName + " at URL '" + url + "' does not exist"
    }
}

/**
 * Reads a resource relative from the calling class.
 *
 * If the URL starts with a `/`, then the resource will be read from the root,
 * so either the JAR root, or from your `resources` directory.
 *
 * If the URL does not start with a `/`, then it is read relative to the package of the calling class.
 */
fun <R> withResource(url: String, block: (InputStream) -> R): R {
    return readResource(url).use(block)
}

/**
 * Computes the missing permissions from the specified permission holder,
 * If you plan on showing them, be sure to use [DefaultMessages.getPermission]
 *
 * @see DefaultMessages.getPermission
 */
fun getMissingPermissions(requiredPerms: EnumSet<Permission>, permissionHolder: IPermissionHolder, channel: GuildChannel): Set<Permission> =
    EnumSet.copyOf(requiredPerms).also { it.removeAll(permissionHolder.getPermissions(channel)) }