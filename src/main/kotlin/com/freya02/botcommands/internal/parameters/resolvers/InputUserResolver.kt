package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.core.entities.InputUser
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.internal.core.entities.InputUserImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

@Resolver
internal class InputUserResolver internal constructor() :
    AbstractUserSnowflakeResolver<InputUserResolver, InputUser>(InputUser::class) {

    override fun transformEntities(user: User, member: Member?): InputUser = InputUserImpl(user, member)
}