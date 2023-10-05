package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.exceptions.BadIdException
import com.freya02.botcommands.api.commands.prefixed.exceptions.NoIdException
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.api.core.utils.logger
import com.freya02.botcommands.api.utils.RichTextFinder
import com.freya02.botcommands.api.utils.RichTextFinder.RichText
import com.freya02.botcommands.api.utils.RichTextType
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.findEntity
import com.freya02.botcommands.internal.core.BContextImpl
import dev.minn.jda.ktx.coroutines.await
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.internal.utils.Helpers

private val logger = KotlinLogging.logger<CommandEvent>()

internal class CommandEventImpl private constructor(
    context: BContextImpl,
    private val event: MessageReceivedEvent,
    argumentsStr: String?,
    private val arguments: MutableList<Any>,
    cancellableRateLimit: CancellableRateLimit
) : CommandEvent(context, event, argumentsStr, cancellableRateLimit) {
    override fun getArguments(): List<Any> = arguments

    override fun <T> hasNext(clazz: Class<T>): Boolean {
        if (arguments.isEmpty()) return false

        val o = arguments.first()
        return clazz.isAssignableFrom(o.javaClass)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> peekArgument(clazz: Class<T>): T {
        if (arguments.isEmpty()) throw NoSuchElementException()

        val o = arguments.first()
        return when {
            clazz.isAssignableFrom(o.javaClass) -> o as T
            else -> throw NoSuchElementException()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> nextArgument(clazz: Class<T>): T {
        if (arguments.isEmpty()) throw NoSuchElementException()

        val o = arguments.removeFirst()
        return when {
            clazz.isAssignableFrom(o.javaClass) -> o as T
            else -> throw NoSuchElementException()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(NoIdException::class, BadIdException::class)
    override fun <T : IMentionable> resolveNext(vararg classes: Class<*>): T {
        if (arguments.isEmpty()) {
            throw NoIdException()
        }

        val o = arguments.removeFirst()
        for (c in classes) {
            if (c.isAssignableFrom(o.javaClass)) {
                return o as T
            }
        }

        if (o !is String) throw NoIdException()

        for (clazz in classes) {
            try {
                //See net.dv8tion.jda.internal.utils.Checks#isSnowflake(String)
                if (o.length > 20 || !Helpers.isNumeric(o)) {
                    throw BadIdException()
                }

                val id = o.toLong()
                val mentionable = when (clazz) {
                    Role::class.java -> guild.getRoleById(id)
                    User::class.java -> findEntity(id, event.message.mentions.users) { jda.retrieveUserById(id).complete() }
                    Member::class.java -> findEntity(id, event.message.mentions.members) { guild.retrieveMemberById(id).complete() }
                    TextChannel::class.java -> guild.getTextChannelById(id)
                    CustomEmoji::class.java -> jda.getEmojiById(id)
                    else -> throw IllegalArgumentException("${clazz.simpleName} is not a valid IMentionable class")
                }

                if (mentionable != null) {
                    return mentionable as T
                }
            } catch (ignored: NumberFormatException) {
                throw BadIdException()
            } catch (e: ErrorResponseException) {
                if (e.errorResponse == ErrorResponse.UNKNOWN_USER || e.errorResponse == ErrorResponse.UNKNOWN_MEMBER) {
                    throw BadIdException()
                } else {
                    throw e
                }
            }
        }
        throw BadIdException()
    }

    companion object {
        private val idRegex = Regex("(\\d+)")

        private operator fun RichText.component1(): String = substring

        private suspend fun tryGetId(mention: String, idToMentionableFunc: suspend (Long) -> IMentionable?): IMentionable? {
            return idRegex.find(mention)
                ?.value
                ?.toLong()
                ?.let { idToMentionableFunc(it) }
        }
        private operator fun RichText.component2(): RichTextType = type

        internal suspend fun create(
            context: BContextImpl,
            event: MessageReceivedEvent,
            argumentsStr: String?,
            cancellableRateLimit: CancellableRateLimit
        ): CommandEventImpl {
            val arguments: MutableList<Any> = arrayListOf()
            RichTextFinder(argumentsStr, true, false, true, false)
                .normalMentionMap
                .values
                .forEach { (substring, type) ->
                    processText(arguments, event.guild, substring, type)
                }

            return CommandEventImpl(context, event, argumentsStr, arguments, cancellableRateLimit)
        }

        private suspend fun processText(arguments: MutableList<Any>, guild: Guild, substring: String, type: RichTextType) {
            if (substring.isBlank()) return

            val mentionType = type.mentionType
            if (mentionType != null || type == RichTextType.UNICODE_EMOTE) {
                val mentionable: Any? = when {
                    type == RichTextType.UNICODE_EMOTE -> Emoji.fromUnicode(substring)
                    mentionType == MentionType.ROLE -> tryGetId(substring) { id: Long ->
                        guild.getRoleById(id)
                    }
                    mentionType == MentionType.CHANNEL -> tryGetId(substring) { id: Long ->
                        guild.getTextChannelById(id)
                    }
                    mentionType == MentionType.EMOJI -> MentionType.EMOJI.pattern.toRegex().find(substring)?.let {
                        it.groups[2]?.value?.let { id -> guild.getEmojiById(id) }
                    }
                    mentionType == MentionType.USER -> tryGetId(substring) { id: Long ->
                        guild.jda.retrieveUserById(id).await()
                    }
                    else -> null
                }

                if (mentionable != null) {
                    arguments.add(mentionable)
                } else {
                    logger.error(
                        "Unresolved mentionable : '{}' of type {}, maybe you haven't enabled a cache flag / intent ?",
                        substring,
                        type.name
                    )
                }
            } else if (substring.isNotEmpty()) {
                arguments.addAll(substring.split(' ').dropLastWhile { it.isEmpty() })
            }
        }
    }
}