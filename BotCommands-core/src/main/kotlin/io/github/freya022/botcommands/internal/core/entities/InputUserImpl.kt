package io.github.freya022.botcommands.internal.core.entities

import io.github.freya022.botcommands.api.core.entities.InputUser
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class InputUserImpl internal constructor(
    private val user: User,
    override val member: Member?
) : InputUser, User by user {
    constructor(member: Member) : this(member.user, member)
    constructor(user: User) : this(user, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InputUserImpl) return false

        return when {
            member != null || other.member != null -> member == other.member
            else -> user == other.user
        }
    }

    override fun hashCode(): Int {
        return when {
            member != null -> member.hashCode()
            else -> user.hashCode()
        }
    }

    override fun toString(): String {
        return when {
            member != null -> "InputUser($member)"
            else -> "InputUser($user)"
        }
    }
}
