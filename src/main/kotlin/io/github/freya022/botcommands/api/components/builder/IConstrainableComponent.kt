package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake

/**
 * Allows components to have constraints
 *
 * @see InteractionConstraints
 */
interface IConstrainableComponent<T : IConstrainableComponent<T>> : BuilderInstanceHolder<T> {
    var constraints: InteractionConstraints

    fun constraints(constraints: InteractionConstraints): T = instance.also {
        this.constraints = constraints
    }

    /** Adds user IDs to the constraints */
    fun addUserIds(vararg userIds: Long): T = addUserIds(userIds.asList())
    /** Adds role IDs to the constraints */
    fun addRoleIds(vararg roleIds: Long): T = addRoleIds(roleIds.asList())
    /** Adds permissions to the constraints */
    fun addPermissions(vararg permissions: Permission): T = addPermissions(permissions.asList())

    /** Adds user IDs to the constraints */
    fun addUsers(vararg users: UserSnowflake): T = addUsers(users.asList())
    /** Adds role IDs to the constraints */
    fun addRoles(vararg roles: Role): T = addRoles(roles.asList())

    /** Adds user IDs to the constraints */
    fun addUsers(users: Collection<UserSnowflake>): T
    /** Adds role IDs to the constraints */
    fun addRoles(roles: Collection<Role>): T

    /** Adds user IDs to the constraints */
    fun addUserIds(userIds: Collection<Long>): T
    /** Adds role IDs to the constraints */
    fun addRoleIds(roleIds: Collection<Long>): T
    /** Adds permissions to the constraints */
    fun addPermissions(permissions: Collection<Permission>): T

    /** Allows manipulating the [InteractionConstraints] instance */
    fun constraints(block: ReceiverConsumer<InteractionConstraints>): T
}