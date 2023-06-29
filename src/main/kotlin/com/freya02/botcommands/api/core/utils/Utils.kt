package com.freya02.botcommands.api.core.utils

import com.freya02.botcommands.api.DefaultMessages
import mu.KLogger
import mu.KotlinLogging
import mu.toKLogger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.slf4j.LoggerFactory
import java.util.*

@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> KotlinLogging.logger(): KLogger =
    LoggerFactory.getLogger(T::class.java).toKLogger()

/**
 * Computes the missing permissions from the specified permission holder,
 * If you plan on showing them, be sure to use [DefaultMessages.getPermission]
 *
 * @see DefaultMessages.getPermission
 */
fun getMissingPermissions(requiredPerms: EnumSet<Permission>, permissionHolder: IPermissionHolder, channel: GuildChannel): Set<Permission> =
    EnumSet.copyOf(requiredPerms).also { it.removeAll(permissionHolder.getPermissions(channel)) }