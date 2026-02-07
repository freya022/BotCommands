package dev.freya02.botcommands.restarter.internal.watcher

import dev.freya02.botcommands.restarter.internal.Restarter
import dev.freya02.botcommands.restarter.internal.sources.SourceDirectoriesListener
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

private val logger = KotlinLogging.logger { }

internal class ClasspathListener internal constructor(
    private val delay: Duration
) : SourceDirectoriesListener {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private lateinit var scheduledRestart: ScheduledFuture<*>

    private val commands: MutableList<() -> Unit> = arrayListOf()

    override fun onChange(command: () -> Unit) {
        commands += command
        if (::scheduledRestart.isInitialized) scheduledRestart.cancel(false)

        scheduledRestart = scheduler.schedule({
            commands.forEach { it.invoke() }
            commands.clear()

            try {
                Restarter.instance.restart()
            } catch (e: Exception) {
                logger.error(e) { "Restart failed, waiting for the next build" }
            }
            scheduler.shutdown()
        }, delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }

    override fun onCancel() {
        scheduler.shutdownNow()
    }
}
