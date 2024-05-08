package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.resolverFactory
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

internal class MemberResolver internal constructor(
    context: BContext
) : AbstractUserSnowflakeResolver<MemberResolver, Member>(context, Member::class) {
    override fun transformEntities(user: User, member: Member?): Member? = member
}

@BService
internal data object MemberResolverFactoryProvider {
    @ResolverFactory
    internal fun memberResolverFactory(context: BContext) = resolverFactory { request ->
        request.checkGuildOnly(Member::class)
        MemberResolver(context)
    }
}
