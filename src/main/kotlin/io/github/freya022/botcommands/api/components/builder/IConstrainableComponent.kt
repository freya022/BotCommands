package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake
import javax.annotation.CheckReturnValue

/**
 * Allows components to have constraints
 *
 * @see InteractionConstraints
 */
interface IConstrainableComponent<T : IConstrainableComponent<T>> : BuilderInstanceHolder<T> {
    var constraints: InteractionConstraints

    /**
     * Replaces the current interaction constraints with the given ones.
     */
    @CheckReturnValue
    fun constraints(constraints: InteractionConstraints): T = instance.also {
        this.constraints = constraints
    }

    /** Adds user IDs to the constraints */
    @CheckReturnValue
    fun addUserIds(vararg userIds: Long): T = addUserIds(userIds.asList())
    /** Adds role IDs to the constraints */
    @CheckReturnValue
    fun addRoleIds(vararg roleIds: Long): T = addRoleIds(roleIds.asList())
    /** Adds permissions to the constraints */
    @CheckReturnValue
    fun addPermissions(vararg permissions: Permission): T = addPermissions(permissions.asList())

    /** Adds user IDs to the constraints */
    @CheckReturnValue
    fun addUsers(vararg users: UserSnowflake): T = addUsers(users.asList())
    /** Adds role IDs to the constraints */
    @CheckReturnValue
    fun addRoles(vararg roles: Role): T = addRoles(roles.asList())

    /** Adds user IDs to the constraints */
    @CheckReturnValue
    fun addUsers(users: Collection<UserSnowflake>): T
    /** Adds role IDs to the constraints */
    @CheckReturnValue
    fun addRoles(roles: Collection<Role>): T

    /** Adds user IDs to the constraints */
    @CheckReturnValue
    fun addUserIds(userIds: Collection<Long>): T
    /** Adds role IDs to the constraints */
    @CheckReturnValue
    fun addRoleIds(roleIds: Collection<Long>): T
    /** Adds permissions to the constraints */
    @CheckReturnValue
    fun addPermissions(permissions: Collection<Permission>): T

    /** Allows manipulating the [InteractionConstraints] instance */
    @CheckReturnValue
    fun constraints(block: ReceiverConsumer<InteractionConstraints>): T
}