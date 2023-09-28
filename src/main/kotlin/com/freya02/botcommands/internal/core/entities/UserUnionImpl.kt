package com.freya02.botcommands.internal.core.entities

import com.freya02.botcommands.api.core.entities.UserUnion
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

internal class UserUnionImpl internal constructor(
    user: User,
    override val member: Member?
) : UserUnion, User by user {
    constructor(member: Member) : this(member.user, member)
    constructor(user: User) : this(user, null)
}