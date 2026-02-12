package dev.freya02.botcommands.jda.keepalive.internal

import dev.freya02.botcommands.jda.keepalive.internal.utils.isJvmShuttingDown
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import java.util.function.Supplier

private val logger = KotlinLogging.logger { }

// TODO there may be an issue with REST requests,
//  as the instance will not get shut down, the requester will still run any request currently queued
//  so we should find a way to cancel the tasks in the rate limiter

// TODO a similar feature exists at https://github.com/LorittaBot/DeviousJDA/blob/master/src/examples/java/SessionCheckpointAndGatewayResumeExample.kt
//  however as it is a JDA fork, users will not be able to use the latest features,
//  there is also a risk that the saved data (checkpoint) could miss fields

// TODO another way of building this feature is to have the user use an external gateway proxy, such as https://github.com/Gelbpunkt/gateway-proxy
//  however such a solution introduces a lot of friction,
//  requiring to set up JDA manually, though not complicated, but also docker and that container's config
//  An hybrid way would require rewriting that proxy,
//  so our module can hook into JDA and set the gateway URL to the proxy's
internal class JDABuilderSession private constructor(
    @get:DynamicCall val key: String,
) {

    @get:DynamicCall
    val configuration = JDABuilderConfiguration()
    var wasBuilt: Boolean = false
        private set

    private lateinit var scheduleShutdownSignal: ScheduleShutdownSignalWrapper

    // May also be shutdownNow
    @DynamicCall
    fun onShutdown(instance: JDA, shutdownFunction: Runnable) {
        if (isJvmShuttingDown()) {
            // "scheduleShutdownSignal" isn't there yet if this shutdown is triggered by JDA's shutdown hook
            return shutdownFunction.run()
        }

        if (::scheduleShutdownSignal.isInitialized.not()) {
            logger.error { "Expected BContextImpl#scheduleShutdownSignal to be called before shutdown, doing a full shut down" }
            return shutdownFunction.run()
        }

        // Don't save if this configuration has unsupported values
        if (configuration.hasUnsupportedValues) {
            scheduleShutdownSignal.runFully()
            shutdownFunction.run()
            return logger.debug { "Discarding JDA instance as the configuration had unsupported values (key '$key')" }
        }

        val eventManager = instance.eventManager as? BufferingEventManager
        eventManager?.detach() // If the event manager isn't what we expect, it will be logged when attempting to reuse

        JDACache[key] = JDACache.Data(configuration, instance, shutdownFunction, scheduleShutdownSignal)
    }

    /**
     * Stores the actual code of BContextImpl#scheduleShutdownSignal and the callback it was passed
     *
     * [scheduleShutdownSignalFunction] will be called with [afterShutdownSignal] if JDA does get shut down,
     * but if JDA is reused, only [afterShutdownSignal] is used.
     */
    fun onScheduleShutdownSignal(scheduleShutdownSignalFunction: Runnable, afterShutdownSignal: () -> Unit) {
        this.scheduleShutdownSignal = ScheduleShutdownSignalWrapper(scheduleShutdownSignalFunction, afterShutdownSignal)
    }

    @DynamicCall
    fun onBuild(buildFunction: Supplier<JDA>): JDA {
        val jda = buildOrReuse(buildFunction)
        wasBuilt = true
        return jda
    }

    private fun buildOrReuse(buildFunction: Supplier<JDA>): JDA {
        val cachedData = JDACache.remove(key)

        fun createNewInstance(): JDA {
            val jda = buildFunction.get()
            cachedData?.scheduleShutdownSignal?.runFully()
            cachedData?.doShutdown?.run()
            return jda
        }

        if (configuration.hasUnsupportedValues) {
            logger.debug { "Configured JDABuilder has unsupported values, building a new JDA instance (key '$key')" }
            return createNewInstance()
        }

        if (cachedData == null) {
            logger.debug { "Creating a new JDA instance (key '$key')" }
            return createNewInstance()
        }

        if (cachedData.configuration isSameAs configuration) {
            logger.debug { "Reusing JDA instance with compatible configuration (key '$key')" }
            val jda = cachedData.jda
            val eventManager = jda.eventManager as? BufferingEventManager
                ?: run {
                    logger.warn { "Expected a BufferingEventManager but got a ${jda.eventManager.javaClass.name}, creating a new instance" }
                    return createNewInstance()
                }

            cachedData.scheduleShutdownSignal.runAfterShutdownSignal()

            eventManager.setDelegate(configuration.eventManager)
            eventManager.handle(StatusChangeEvent(jda, JDA.Status.LOADING_SUBSYSTEMS, JDA.Status.CONNECTED))
            jda.guildCache.forEachUnordered { eventManager.handle(GuildReadyEvent(jda, -1, it)) }
            eventManager.handle(ReadyEvent(jda))
            return jda
        } else {
            logger.debug { "Creating a new JDA instance as its configuration changed (key '$key')" }
            return createNewInstance()
        }
    }

    internal class ScheduleShutdownSignalWrapper internal constructor(
        private val scheduleShutdownSignalFunction: Runnable,
        private val afterShutdownSignal: () -> Unit
    ) {

        internal fun runFully(): Unit = scheduleShutdownSignalFunction.run()

        internal fun runAfterShutdownSignal(): Unit = afterShutdownSignal()
    }

    companion object {
        // I would store them in a Map, but JDABuilder has no idea what the key is
        private val activeSession: ThreadLocal<JDABuilderSession> =
            ThreadLocal.withInitial { error("No JDABuilderSession exists for this thread") }

        private val sessions: MutableMap<String, JDABuilderSession> = hashMapOf()

        @JvmStatic
        @DynamicCall
        fun currentSession(): JDABuilderSession = activeSession.get()

        @JvmStatic
        @DynamicCall
        fun getSession(key: String): JDABuilderSession {
            return sessions[key] ?: error("No JDABuilderSession exists for key '$key'")
        }

        @JvmStatic
        @DynamicCall
        fun withBuilderSession(
            key: String,
            // Use Java function types to make codegen a bit more reliable
            block: Runnable
        ) {
            val session = JDABuilderSession(key)
            sessions[key] = session
            activeSession.set(session)
            try {
                block.run()
                if (!session.wasBuilt) {
                    logger.warn { "Could not save/restore any JDA session as none were built" }
                }
            } finally {
                activeSession.remove()
            }
        }
    }
}
