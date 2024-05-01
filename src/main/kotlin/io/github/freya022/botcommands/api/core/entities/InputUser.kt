package io.github.freya022.botcommands.api.core.entities

import io.github.freya022.botcommands.internal.core.entities.InputUserImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

/**
 * Extension of [User] containing a nullable [Member], obtainable on text command and all interactions.
 *
 * @see User.asInputUser
 * @see Member.asInputUser
 */
interface InputUser : User {
    /**
     * Returns the member object of this user, based on the event's context,
     * or `null` if the user is not in the guild.
     */
    val member: Member?
}

/**
 * Wraps this user as an [InputUser].
 */
fun User.asInputUser(): InputUser = InputUserImpl(this)
/**
 * Wraps this member as an [InputUser].
 */
fun Member.asInputUser(): InputUser = InputUserImpl(this)