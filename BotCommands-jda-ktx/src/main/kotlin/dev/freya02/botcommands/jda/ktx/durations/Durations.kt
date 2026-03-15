package dev.freya02.botcommands.jda.ktx.durations

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.BulkBanResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.Once
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import net.dv8tion.jda.api.utils.concurrent.Task
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask
import okhttp3.MediaType
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * @see RestAction.delay
 */
fun <T> RestAction<T>.delay(duration: Duration): RestAction<T> =
    delay(duration.toJavaDuration())

/**
 * @see RestAction.delay
 */
fun <T> RestAction<T>.delay(duration: Duration, scheduler: ScheduledExecutorService): RestAction<T> =
    delay(duration.toJavaDuration(), scheduler)

/**
 * @see RestAction.timeout
 */
@Suppress("UNCHECKED_CAST")
fun <T : RestAction<*>> T.timeout(duration: Duration): T =
    timeout(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS) as T

/**
 * @see JDA.awaitShutdown
 */
fun JDA.awaitShutdown(timeout: Duration): Boolean = awaitShutdown(timeout.toJavaDuration())

/**
 * @see Guild.timeoutFor
 */
fun Guild.timeoutFor(user: UserSnowflake, duration: Duration): AuditableRestAction<Void> = timeoutFor(user, duration.toJavaDuration())
/**
 * @see Member.timeoutFor
 */
fun Member.timeoutFor(duration: Duration): AuditableRestAction<Void> = timeoutFor(duration.toJavaDuration())

/**
 * @see Member.ban
 */
fun Member.ban(deletionTimeframe: Duration): AuditableRestAction<Void> =
    ban(deletionTimeframe.inWholeSeconds.toInt(), TimeUnit.SECONDS)

/**
 * @see Guild.ban
 */
fun Guild.ban(user: UserSnowflake, deletionTimeframe: Duration): AuditableRestAction<Void> =
    ban(user, deletionTimeframe.inWholeSeconds.toInt(), TimeUnit.SECONDS)

/**
 * See [bulk ban from JDA](https://docs.jda.wiki/net/dv8tion/jda/api/entities/Guild.html#ban(java.util.Collection,java.time.Duration))
 */
fun Guild.ban(users: Collection<UserSnowflake>, duration: Duration): AuditableRestAction<BulkBanResponse> =
    ban(users, duration.toJavaDuration())

/**
 * @see TimeFormat.after
 */
fun TimeFormat.after(duration: Duration): Timestamp = after(duration.toJavaDuration())
/**
 * @see TimeFormat.before
 */
fun TimeFormat.before(duration: Duration): Timestamp = before(duration.toJavaDuration())

/**
 * @see Timestamp.plus
 */
operator fun Timestamp.plus(duration: Duration): Timestamp = plus(duration.toJavaDuration())
/**
 * @see Timestamp.minus
 */
operator fun Timestamp.minus(duration: Duration): Timestamp = minus(duration.toJavaDuration())

/**
 * @see Task.setTimeout
 */
fun <T> Task<T>.setTimeout(duration: Duration): Task<T> = setTimeout(duration.toJavaDuration())
/**
 * @see GatewayTask.setTimeout
 */
fun <T> GatewayTask<T>.setTimeout(duration: Duration): Task<T> = setTimeout(duration.toJavaDuration())

/**
 * @see FileUpload.asVoiceMessage
 */
fun FileUpload.asVoiceMessage(mediaType: MediaType, waveform: ByteArray, duration: Duration): FileUpload =
    asVoiceMessage(mediaType, waveform, duration.toJavaDuration())

/**
 * @see Once.Builder.timeout
 */
fun <E : GenericEvent> Once.Builder<E>.timeout(timeout: Duration): Once.Builder<E> =
    timeout(timeout.toJavaDuration())

/**
 * @see Once.Builder.timeout
 */
fun <E : GenericEvent> Once.Builder<E>.timeout(timeout: Duration, timeoutCallback: Runnable): Once.Builder<E> =
    timeout(timeout.toJavaDuration(), timeoutCallback)

/**
 * @see MessagePollBuilder.setDuration
 */
fun MessagePollBuilder.setDuration(duration: Duration): MessagePollBuilder =
    setDuration(duration.toJavaDuration())
