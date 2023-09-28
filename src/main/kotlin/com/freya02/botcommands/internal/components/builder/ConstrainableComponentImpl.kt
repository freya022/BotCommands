package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.builder.IConstrainableComponent
import com.freya02.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake

internal class ConstrainableComponentImpl : IConstrainableComponent {
    override var constraints: InteractionConstraints = InteractionConstraints.empty()

    override fun constraints(block: ReceiverConsumer<InteractionConstraints>) {
        constraints.apply(block)
    }

    override fun addUsers(users: Collection<UserSnowflake>) {
        constraints.addUsers(users)
    }

    override fun addRoles(roles: Collection<Role>) {
        constraints.addRoles(roles)
    }

    override fun addUserIds(userIds: Collection<Long>) {
        constraints.addUserIds(userIds)
    }

    override fun addRoleIds(roleIds: Collection<Long>) {
        constraints.addRoleIds(roleIds)
    }

    override fun addPermissions(permissions: Collection<Permission>) {
        constraints.addPermissions(permissions)
    }
}