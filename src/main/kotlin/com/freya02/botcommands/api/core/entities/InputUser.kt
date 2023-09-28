package com.freya02.botcommands.api.core.entities

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

/**
 * Extension of [User] containing a nullable [Member], obtainable on text command and all interactions.
 */
interface InputUser : User {
    /**
     * Returns the member object of this user, based on the event's context,
     * or `null` if the user is not in the guild.
     */
    val member: Member?
}