package io.github.freya022.botcommands.api.components.data

import gnu.trove.list.array.TLongArrayList
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Controls who can use components (buttons, selection menus).
 *
 * A user can use a component if they fit *at least* one of the allowed elements.
 *
 * You can filter by:
 * - User ID
 * - Role ID
 * - Permissions
 *
 * You can create interaction constraints from the static methods,
 * or by using the existing instance in the component builders.
 */
class InteractionConstraints private constructor() {
    val allowedUsers = TLongArrayList()
    val allowedRoles = TLongArrayList()
    val allowingPermissions = enumSetOf<Permission>()

    val isEmpty: Boolean
        get() = allowedUsers.isEmpty && allowedRoles.isEmpty && allowingPermissions.isEmpty()

    fun addUserIds(vararg userIds: Long): InteractionConstraints = apply {
        allowedUsers.addAll(userIds)
    }

    fun addUserIds(userIds: Collection<Long>): InteractionConstraints = apply {
        allowedUsers.addAll(userIds)
    }

    fun addUsers(vararg userIds: UserSnowflake): InteractionConstraints = apply {
        for (user in userIds) {
            allowedUsers.add(user.idLong)
        }
    }

    fun addUsers(users: Collection<UserSnowflake>): InteractionConstraints = apply {
        for (user in users) {
            allowedUsers.add(user.idLong)
        }
    }

    fun addRoleIds(vararg roleIds: Long): InteractionConstraints = apply {
        allowedRoles.addAll(roleIds)
    }

    fun addRoleIds(roleIds: Collection<Long>): InteractionConstraints = apply {
        allowedRoles.addAll(roleIds)
    }

    fun addRoles(vararg roles: Role): InteractionConstraints = apply {
        for (role in roles) {
            allowedRoles.add(role.idLong)
        }
    }

    fun addRoles(roles: Collection<Role>): InteractionConstraints = apply {
        for (role in roles) {
            allowedRoles.add(role.idLong)
        }
    }

    fun addPermissions(vararg permissions: Permission): InteractionConstraints = apply {
        allowingPermissions.addAll(permissions)
    }

    fun addPermissions(permissions: Collection<Permission>): InteractionConstraints = apply {
        allowingPermissions.addAll(permissions)
    }

    fun setConstraints(otherConstraints: InteractionConstraints): InteractionConstraints = apply {
        allowedUsers.clear()
        allowedRoles.clear()
        allowingPermissions.clear()
        allowedUsers.addAll(otherConstraints.allowedUsers)
        allowedRoles.addAll(otherConstraints.allowedRoles)
        allowingPermissions.addAll(otherConstraints.allowingPermissions)
    }

    @JvmSynthetic
    operator fun plusAssign(role: Role) {
        addRoles(role)
    }

    @JvmSynthetic
    @JvmName("plusAssignRoles")
    operator fun plusAssign(roles: Collection<Role>) {
        addRoles(roles)
    }

    @JvmSynthetic
    operator fun plusAssign(user: UserSnowflake) {
        addUsers(user)
    }

    @JvmSynthetic
    @JvmName("plusAssignUsers")
    operator fun plusAssign(users: Collection<UserSnowflake>) {
        addUsers(users)
    }

    @JvmSynthetic
    operator fun plusAssign(permission: Permission) {
        addPermissions(permission)
    }

    @JvmSynthetic
    @JvmName("plusAssignPermission")
    operator fun plusAssign(permissions: Collection<Permission>) {
        addPermissions(permissions)
    }

    @JvmSynthetic
    internal fun isAllowed(event: GenericComponentInteractionCreateEvent): Boolean {
        if (isEmpty) return true

        if (event.user.idLong in allowedUsers) return true

        val member = event.member
        if (member != null) {
            if (allowingPermissions.isNotEmpty()) {
                if (member.hasPermission(event.guildChannel, allowingPermissions)) {
                    return true
                }
            }

            //If the member has any of these roles
            if (member.roles.any { it.idLong in allowedRoles }) {
                return true
            }
        }

        return false
    }

    companion object {
        @JvmStatic
        fun empty(): InteractionConstraints {
            return InteractionConstraints()
        }

        @JvmStatic
        fun ofUserIds(vararg userIds: Long): InteractionConstraints {
            return empty().addUserIds(*userIds)
        }

        @JvmStatic
        fun ofUserIds(userIds: Collection<Long>): InteractionConstraints {
            return empty().addUserIds(userIds)
        }

        @JvmStatic
        fun ofUsers(vararg users: UserSnowflake): InteractionConstraints {
            return empty().addUsers(*users)
        }

        @JvmStatic
        fun ofUsers(users: Collection<UserSnowflake>): InteractionConstraints {
            return empty().addUsers(users)
        }

        @JvmStatic
        fun ofRoleIds(vararg roleIds: Long): InteractionConstraints {
            return empty().addRoleIds(*roleIds)
        }

        @JvmStatic
        fun ofRoleIds(roleIds: Collection<Long>): InteractionConstraints {
            return empty().addRoleIds(roleIds)
        }

        @JvmStatic
        fun ofRoles(vararg roles: Role): InteractionConstraints {
            return empty().addRoles(*roles)
        }

        @JvmStatic
        fun ofRoles(roles: Collection<Role>): InteractionConstraints {
            return empty().addRoles(roles)
        }

        @JvmStatic
        fun ofPermissions(vararg permissions: Permission): InteractionConstraints {
            return empty().addPermissions(*permissions)
        }

        @JvmSynthetic
        internal fun of(userIds: List<Long>, roleIds: List<Long>, rawPermissions: Long): InteractionConstraints = empty().apply {
            addUserIds(userIds)
            addRoleIds(roleIds)
            addPermissions(Permission.getPermissions(rawPermissions))
        }
    }
}