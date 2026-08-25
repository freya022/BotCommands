package dev.freya02.botcommands.restarter.internal.watcher

import dev.freya02.botcommands.restarter.internal.Restarter
import dev.freya02.botcommands.restarter.internal.sources.DeletedSourceFile
import dev.freya02.botcommands.restarter.internal.sources.SourceFile
import dev.freya02.botcommands.restarter.internal.sources.SourceFiles
import dev.freya02.botcommands.restarter.internal.sources.plus
import dev.freya02.botcommands.restarter.internal.utils.AppClasspath
import dev.freya02.botcommands.restarter.internal.utils.walkDirectories
import dev.freya02.botcommands.restarter.internal.utils.walkFiles
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

private val logger = KotlinLogging.logger { }

internal class ClasspathWatcher private constructor() {

    private val registrationStatus = RegistrationStatus()

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var restartFuture: Future<*> = CompletableFuture.completedFuture(null)

    private val watchService = FileSystems.getDefault().newWatchService()
    private val registeredDirectories: MutableSet<Path> = ConcurrentHashMap.newKeySet()
    private val snapshots: MutableMap<Path, SourceFiles> = hashMapOf()

    init {
        AppClasspath.paths.forEach { classRoot ->
            require(classRoot.isDirectory())

            logger.trace { "Creating snapshot of ${classRoot.absolutePathString()}" }
            snapshots[classRoot] = classRoot.takeSnapshot()

            logger.trace { "Listening to ${classRoot.absolutePathString()}" }
            registerDirectories(classRoot)
        }

        val _ = thread(name = "Classpath watcher", isDaemon = true) {
            while (true) {
                val key = try {
                    watchService.take() // Wait for a change
                } catch (_: InterruptedException) {
                    return@thread logger.trace { "Interrupted watching classpath" }
                }
                val pollEvents = key.pollEvents()
                if (pollEvents.isNotEmpty()) {
                    logger.trace {
                        val affectedList = pollEvents.joinAsList { "${it.kind()}: ${it.context()}" }
                        "Affected files:\n$affectedList"
                    }
                } else {
                    // Seems to be empty when a directory gets deleted
                    // The next watch key *should* be an ENTRY_DELETE of that directory
                    continue
                }
                if (!key.reset()) {
                    logger.debug { "${key.watchable()} is no longer valid" }
                    continue
                }

                restartFuture.cancel(/* mayInterruptIfRunning = */ false)
                restartFuture = scheduler.schedule(
                    ::tryRestart,
                    Restarter.config.restartDelay.inWholeMilliseconds,
                    TimeUnit.MILLISECONDS
                )
            }
        }
    }

    /**
     * Tries to restart immediately, if no instance is registered (i.e., ready for restarts),
     * this will wait until one is.
     *
     * When the restart is attempted, further restart attempts will wait for the current one to finish,
     * then, classpath content is checked for changes, throwing if there were none.
     *
     * Finally, new directories will be watched and the app restarts.
     *
     * Any exception thrown are caught and will cause more classpath changes to be awaited for a new restart attempt
     */
    private fun tryRestart() {
        // Wait for a BC instance to register itself before attempting a restart
        registrationStatus.awaitIfAbsent()
        try {
            logger.debug { "Attempting to restart" }

            // Prevent restarts until a new BC instance gets registered
            registrationStatus.setAbsent()

            compareSnapshots()
            snapshots.keys.forEach { registerDirectories(it) }

            val exception = Restarter.instance.restart()
            if (exception != null) throw exception
        } catch (e: Exception) {
            logger.error(e) { "Restart failed, waiting for the next build" }
            // Signal we listen to builds again
            registrationStatus.setPresent()
        }
    }

    private fun compareSnapshots() {
        val hasChanges = snapshots.any { (directory, files) ->
            val snapshot = directory.takeSnapshot()

            // Exclude deleted files so they don't count as being deleted again
            val deletedPaths = files.withoutDeletes().keys - snapshot.keys
            if (deletedPaths.isNotEmpty()) {
                logger.info { "${deletedPaths.size} files were deleted in ${directory.absolutePathString()}: $deletedPaths" }
                snapshots[directory] = deletedPaths.associateWith { DeletedSourceFile } + snapshot
                // So we can re-register them in case they are recreated
                registeredDirectories.removeAll(deletedPaths.map { directory.resolve(it) })
                return@any true
            }

            // Exclude deleted files so they count as being added back
            val addedPaths = snapshot.keys - files.withoutDeletes().keys
            if (addedPaths.isNotEmpty()) {
                logger.info { "${addedPaths.size} files were added in ${directory.absolutePathString()}: $addedPaths" }
                snapshots[directory] =  files + snapshot
                return@any true
            }

            val modifiedFiles = snapshot.keys.filter { key ->
                val actual = snapshot[key] ?: error("Key from map is missing a value somehow")
                val expected = files[key] ?: error("Expected file is missing, should have been detected as deleted")

                // File was deleted (on the 2nd build for example) and got recreated (on the 3rd build for example)
                if (expected is DeletedSourceFile) error("Expected file was registered as deleted, should have been detected as added")
                expected as SourceFile

                actual as SourceFile // Assertion

                actual.lastModified != expected.lastModified
            }
            if (modifiedFiles.isNotEmpty()) {
                logger.info { "${modifiedFiles.size} files were modified in ${directory.absolutePathString()}: $modifiedFiles" }
                snapshots[directory] = files + snapshot
                return@any true
            }

            false
        }

        if (!hasChanges)
            error("Received a file system event but no changes were detected")
    }

    private fun registerDirectories(directory: Path) {
        directory.walkDirectories { path, _ ->
            if (registeredDirectories.add(path))
                path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        }
    }

    /**
     * Helper class to await for a BC instance to be registered before restarting
     */
    private class RegistrationStatus {
        private var present = true

        private val lock = ReentrantLock()
        private val condition = lock.newCondition()

        fun setPresent() = lock.withLock {
            this.present = true
            condition.signalAll()
        }

        fun setAbsent() = lock.withLock { present = false }

        fun awaitIfAbsent(): Unit = lock.withLock {
            if (present) return@withLock

            condition.await()
        }
    }

    internal companion object {
        internal lateinit var instance: ClasspathWatcher
            private set

        @Synchronized
        internal fun initialize() {
            if (::instance.isInitialized.not()) {
                instance = ClasspathWatcher()
            } else {
                instance.registrationStatus.setPresent()
            }
        }
    }
}

private fun Path.takeSnapshot(): SourceFiles = walkFiles().associate { (it, attrs) ->
    it.relativeTo(this).pathString to SourceFile(attrs.lastModifiedTime().toInstant())
}.let(::SourceFiles)
