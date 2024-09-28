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
    IConstrainableComponentMixin<T> {

    override var constraints: InteractionConstraints = InteractionConstraints.empty()

    override fun constraints(block: ReceiverConsumer<InteractionConstraints>): T = applyInstance {
        constraints.apply(block)
    }

    override fun constraints(constraints: InteractionConstraints): T = applyInstance {
        this.constraints = constraints
    }

    override fun addUsers(users: Collection<UserSnowflake>): T = applyInstance {
        constraints.addUsers(users)
    }

    override fun addRoles(roles: Collection<Role>): T = applyInstance {
        constraints.addRoles(roles)
    }

    override fun addUserIds(userIds: Collection<Long>): T = applyInstance {
        constraints.addUserIds(userIds)
    }

    override fun addRoleIds(roleIds: Collection<Long>): T = applyInstance {
        constraints.addRoleIds(roleIds)
    }

    override fun addPermissions(permissions: Collection<Permission>): T = applyInstance {
        constraints.addPermissions(permissions)
    }
}