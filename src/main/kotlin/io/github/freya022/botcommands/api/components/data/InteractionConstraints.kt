package io.github.freya022.botcommands.api.components.data

import gnu.trove.list.array.TLongArrayList
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.*

/**
 * Controls who can use interactions such as components (button, selection menu)
 *
 * This acts like a white list, if a user or a member has **at least one** of the requirements, he can use the interaction
 *
 * You can filter by:
 *
 *  * User ID
 *  * Role ID
 *  * Permissions
 *
 * You can create interaction constraints from the static methods or by using the existing instance in the component builders
 */
//TODO reimplement using a separate mutable type
// use a separate implementation class, move isAllowed as an internal extension
class InteractionConstraints private constructor() {
    val userList = TLongArrayList()
    val roleList = TLongArrayList()
    val permissions = enumSetOf<Permission>()

    val isEmpty: Boolean
        get() = userList.isEmpty && roleList.isEmpty && permissions.isEmpty()

    fun addUserIds(vararg userIds: Long): InteractionConstraints = this.also {
        userList.addAll(userIds)
    }

    fun addUserIds(userIds: Collection<Long?>?): InteractionConstraints = this.also {
        userList.addAll(userIds)
    }

    fun addUsers(vararg userIds: UserSnowflake): InteractionConstraints = this.also {
        for (user in userIds) {
            userList.add(user.idLong)
        }
    }

    fun addUsers(users: Collection<UserSnowflake>): InteractionConstraints = this.also {
        for (user in users) {
            userList.add(user.idLong)
        }
    }

    fun addRoleIds(vararg roleIds: Long): InteractionConstraints = this.also {
        roleList.addAll(roleIds)
    }

    fun addRoleIds(roleIds: Collection<Long?>?): InteractionConstraints = this.also {
        roleList.addAll(roleIds)
    }

    fun addRoles(vararg roles: Role): InteractionConstraints = this.also {
        for (role in roles) {
            roleList.add(role.idLong)
        }
    }

    fun addRoles(roles: Collection<Role>): InteractionConstraints = this.also {
        for (role in roles) {
            roleList.add(role.idLong)
        }
    }

    fun addPermissions(vararg permissions: Permission?): InteractionConstraints = this.also {
        Collections.addAll(this.permissions, *permissions)
    }

    fun addPermissions(permissions: Collection<Permission>?): InteractionConstraints = this.also {
        this.permissions.addAll(permissions!!)
    }

    fun setConstraints(otherConstraints: InteractionConstraints): InteractionConstraints = this.also {
        userList.clear()
        roleList.clear()
        permissions.clear()
        userList.addAll(otherConstraints.userList)
        roleList.addAll(otherConstraints.roleList)
        permissions.addAll(otherConstraints.permissions)
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

        if (event.user.idLong in userList) return true

        val member = event.member
        if (member != null) {
            if (permissions.isNotEmpty()) {
                if (member.hasPermission(event.guildChannel, permissions)) {
                    return true
                }
            }

            //If the member has any of these roles
            if (member.roles.any { it.idLong in roleList }) {
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