package io.github.freya022.botcommands.internal.parameters.resolvers.users

import dev.freya02.botcommands.jda.ktx.retrieve.retrieveMemberByIdOrNull
import dev.freya02.botcommands.jda.ktx.retrieve.retrieveUserByIdOrNull
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.traceNull
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.internal.core.entities.InputUserImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Message

private val logger = KotlinLogging.logger { }

abstract class AbstractInputUserResolver<T : AbstractInputUserResolver<T>> :
        ClassParameterResolver<T, InputUser>(InputUser::class) {

    suspend fun retrieveOrNull(userId: Long, message: Message): InputUser? {
        val guild = message.guildOrNull
        val member = when {
            guild != null -> {
                message.mentions.members.findEntity(userId)
                    ?: guild.retrieveMemberByIdOrNull(userId)
            }

            else -> null
        }

        val user = member?.user
            ?: message.mentions.users.findEntity(userId)
            ?: message.jda.retrieveUserByIdOrNull(userId)

        if (user == null) {
            return logger.traceNull { "Could not resolve user with ID $userId in '${guild?.name}' (${guild?.id})" }
        }

        return InputUserImpl(user, member)
    }

    private val Message.guildOrNull get() = if (isFromGuild) guild else null

    private fun <T : ISnowflake> Collection<T>.findEntity(id: Long): T? =
        find { user -> user.idLong == id }
}
