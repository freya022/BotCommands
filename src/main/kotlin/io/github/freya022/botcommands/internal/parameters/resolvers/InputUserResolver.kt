package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.internal.core.entities.InputUserImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

@Resolver
internal class InputUserResolver internal constructor() :
    AbstractUserSnowflakeResolver<InputUserResolver, InputUser>(InputUser::class) {

    override fun transformEntities(user: User, member: Member?): InputUser = InputUserImpl(user, member)
}