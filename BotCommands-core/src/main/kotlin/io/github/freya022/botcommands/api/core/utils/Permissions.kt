package io.github.freya022.botcommands.api.core.utils

import io.github.freya022.botcommands.api.localization.PermissionLocalization
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import java.util.*

/**
 * Computes the missing permissions from the specified permission holder,
 * if you plan on showing them, be sure to use [PermissionLocalization.localize].
 *
 * @see PermissionLocalization.localize
 */
fun getMissingPermissions(requiredPerms: EnumSet<Permission>, permissionHolder: IPermissionHolder, channel: GuildChannel): Set<Permission> =
    requiredPerms.clone().also { it.removeAll(permissionHolder.getPermissions(channel)) }
