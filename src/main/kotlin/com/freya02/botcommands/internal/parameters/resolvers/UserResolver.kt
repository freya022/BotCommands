package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.core.service.annotations.Resolver
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

@Resolver
internal class UserResolver internal constructor(): AbstractUserSnowflakeResolver<UserResolver, User>(User::class) {
    override fun transformEntities(user: User, member: Member?): User = user
}