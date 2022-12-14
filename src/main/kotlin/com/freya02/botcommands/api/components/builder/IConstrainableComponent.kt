package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.data.InteractionConstraints
import net.dv8tion.jda.api.Permission

interface IConstrainableComponent {
    var constraints: InteractionConstraints

    fun addUserIds(vararg userIds: Long) = addUserIds(userIds.asList())
    fun addRoleIds(vararg roleIds: Long) = addRoleIds(roleIds.asList())
    fun addPermissions(vararg permissions: Permission) = addPermissions(permissions.asList())

    fun addUserIds(userIds: Collection<Long>)
    fun addRoleIds(roleIds: Collection<Long>)
    fun addPermissions(permissions: Collection<Permission>)

    fun constraints(block: ReceiverConsumer<InteractionConstraints>)
}