package com.freya02.botcommands.api.core.utils

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

suspend fun Guild.retrieveMemberOrNull(userId: Long): Member? = retrieveMemberOrNull(UserSnowflake.fromId(userId))
suspend fun Guild.retrieveMemberOrNull(user: UserSnowflake): Member? = try {
    retrieveMember(user).await()
} catch (e: ErrorResponseException) {
    if (e.errorResponse != ErrorResponse.UNKNOWN_MEMBER)
        throw e
    null
}

suspend fun JDA.retrieveUserOrNull(userId: Long): User? = try {
    retrieveUserById(userId).await()
} catch (e: ErrorResponseException) {
    if (e.errorResponse != ErrorResponse.UNKNOWN_MEMBER)
        throw e
    null
}