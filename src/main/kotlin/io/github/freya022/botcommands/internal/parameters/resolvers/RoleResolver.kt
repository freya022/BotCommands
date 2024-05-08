package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.resolverFactory
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Role

@BService
internal data object RoleResolverFactoryProvider {
    @ResolverFactory
    internal fun roleResolverFactory() = resolverFactory { request ->
        request.checkGuildOnly(Role::class)
        RoleResolver()
    }
}