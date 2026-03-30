package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.resolverFactory
import net.dv8tion.jda.api.entities.Role

@BService
@ServiceName("componentRoleResolverFactoryProvider")
internal data object RoleResolverFactoryProvider {
    @ResolverFactory
    @ServiceName("componentRoleResolverFactory")
    internal fun roleResolverFactory() = resolverFactory { request ->
        request.checkGuildOnly(Role::class)
        RoleResolver()
    }
}
