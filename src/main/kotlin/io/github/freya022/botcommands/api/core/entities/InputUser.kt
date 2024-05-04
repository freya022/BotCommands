package io.github.freya022.botcommands.api.core.entities

import io.github.freya022.botcommands.internal.core.entities.InputUserImpl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.utils.ImageProxy

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

    override fun getEffectiveName(): String {
        return member?.effectiveName ?: super.getEffectiveName()
    }

    override fun getEffectiveAvatar(): ImageProxy {
        return member?.effectiveAvatar ?: super.getEffectiveAvatar()
    }

    override fun getEffectiveAvatarUrl(): String {
        return member?.effectiveAvatarUrl ?: super.getEffectiveAvatarUrl()
    }

    //TODO effective decoration
}

/**
 * Wraps this user as an [InputUser].
 */
fun User.asInputUser(): InputUser = InputUserImpl(this)
/**
 * Wraps this member as an [InputUser].
 */
fun Member.asInputUser(): InputUser = InputUserImpl(this)

/**
 * Gets the interaction user as an [InputUser].
 */
val Interaction.inputUser: InputUser get() = InputUserImpl(user, member)
/**
 * Gets the message's author as an [InputUser].
 */
val Message.inputUser: InputUser get() = InputUserImpl(author, member)
/**
 * Gets the message's author as an [InputUser].
 */
val MessageReceivedEvent.inputUser: InputUser get() = InputUserImpl(author, member)