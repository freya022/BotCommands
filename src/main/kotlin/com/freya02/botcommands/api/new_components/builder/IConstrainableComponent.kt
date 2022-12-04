package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake

interface IConstrainableComponent {
    var constraints: InteractionConstraints

    fun addUserIds(vararg userIds: Long) = addUserIds(userIds.asList())
    fun addRoleIds(vararg roleIds: Long) = addRoleIds(roleIds.asList())
    fun addPermissions(vararg permissions: Permission) = addPermissions(permissions.asList())

    fun addUserIds(userIds: Collection<Long>)
    fun addRoleIds(roleIds: Collection<Long>)
    fun addPermissions(permissions: Collection<Permission>)

    fun constraints(block: ReceiverConsumer<InteractionConstraints>)

    companion object { //TODO move to IC rewrite
        operator fun InteractionConstraints.plusAssign(role: Role) {
            addRoles(role)
        }

        @JvmName("plusAssignRole")
        operator fun InteractionConstraints.plusAssign(roles: Collection<Role>) {
            addRoles(roles)
        }

        operator fun InteractionConstraints.plusAssign(role: UserSnowflake) {
            addUsers(role)
        }

        @JvmName("plusAssignUser")
        operator fun InteractionConstraints.plusAssign(roles: Collection<UserSnowflake>) {
            addUsers(roles)
        }

        operator fun InteractionConstraints.plusAssign(permission: Permission) {
            addPermissions(permission)
        }

        @JvmName("plusAssignPermission")
        operator fun InteractionConstraints.plusAssign(permissions: Collection<Permission>) {
            addPermissions(permissions)
        }
    }
}