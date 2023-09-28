package com.freya02.botcommands.api.core.entities

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

/**
 * Extension of [User] containing a nullable [Member].
 *
 * This object can be a parameter of text commands and all interactions, both in DM and guild contexts.
 */
interface UserUnion : User {
    /**
     * Returns the member object of this user, based on the event's context,
     * or `null` if the user is not in the guild.
     */
    val member: Member?
}