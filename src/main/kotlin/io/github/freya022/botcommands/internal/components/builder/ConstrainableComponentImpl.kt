package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.builder.IConstrainableComponent
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake

internal class ConstrainableComponentImpl<T : IConstrainableComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IConstrainableComponent<T> {

    override var constraints: InteractionConstraints = InteractionConstraints.empty()

    override fun constraints(block: ReceiverConsumer<InteractionConstraints>): T = instance.also {
        constraints.apply(block)
    }

    override fun addUsers(users: Collection<UserSnowflake>): T = instance.also {
        constraints.addUsers(users)
    }

    override fun addRoles(roles: Collection<Role>): T = instance.also {
        constraints.addRoles(roles)
    }

    override fun addUserIds(userIds: Collection<Long>): T = instance.also {
        constraints.addUserIds(userIds)
    }

    override fun addRoleIds(roleIds: Collection<Long>): T = instance.also {
        constraints.addRoleIds(roleIds)
    }

    override fun addPermissions(permissions: Collection<Permission>): T = instance.also {
        constraints.addPermissions(permissions)
    }
}