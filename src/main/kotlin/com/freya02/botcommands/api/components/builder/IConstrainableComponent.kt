package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake

/**
 * Allows components to have constraints
 *
 * @see InteractionConstraints
 */
interface IConstrainableComponent {
    var constraints: InteractionConstraints

    /** Adds user IDs to the constraints */
    fun addUserIds(vararg userIds: Long) = addUserIds(userIds.asList())
    /** Adds role IDs to the constraints */
    fun addRoleIds(vararg roleIds: Long) = addRoleIds(roleIds.asList())
    /** Adds permissions to the constraints */
    fun addPermissions(vararg permissions: Permission) = addPermissions(permissions.asList())

    /** Adds user IDs to the constraints */
    fun addUsers(vararg users: UserSnowflake) = addUsers(users.asList())
    /** Adds role IDs to the constraints */
    fun addRoles(vararg roles: Role) = addRoles(roles.asList())

    /** Adds user IDs to the constraints */
    fun addUsers(users: Collection<UserSnowflake>)
    /** Adds role IDs to the constraints */
    fun addRoles(roles: Collection<Role>)

    /** Adds user IDs to the constraints */
    fun addUserIds(userIds: Collection<Long>)
    /** Adds role IDs to the constraints */
    fun addRoleIds(roleIds: Collection<Long>)
    /** Adds permissions to the constraints */
    fun addPermissions(permissions: Collection<Permission>)

    /** Allows manipulating the [InteractionConstraints] instance */
    fun constraints(block: ReceiverConsumer<InteractionConstraints>)
}