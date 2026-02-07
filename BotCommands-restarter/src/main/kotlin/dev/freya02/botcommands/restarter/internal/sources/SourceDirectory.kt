package dev.freya02.botcommands.restarter.internal.sources

import dev.freya02.botcommands.restarter.internal.utils.walkDirectories
import dev.freya02.botcommands.restarter.internal.utils.walkFiles
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import kotlin.concurrent.thread
import kotlin.io.path.*

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalPathApi::class)
internal class SourceDirectory internal constructor(
    val directory: Path,
    val files: SourceFiles,
    private val listener: SourceDirectoryListener,
) {

    private val thread: Thread

    init {
        require(directory.isDirectory())

        logger.trace { "Listening to ${directory.absolutePathString()}" }

        val watchService = directory.fileSystem.newWatchService()
        directory.walkDirectories { path, attributes ->
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        }

        thread = thread(name = "Classpath watcher of '${directory.fileName}'", isDaemon = true) {
            try {
                watchService.take() // Wait for a change
            } catch (_: InterruptedException) {
                return@thread logger.trace { "Interrupted watching ${directory.absolutePathString()}" }
            }
            watchService.close()

            listener.onChange(sourcesFilesFactory = {
                val snapshot = directory.takeSnapshot()

                // Exclude deleted files so they don't count as being deleted again
                val deletedPaths = files.withoutDeletes().keys - snapshot.keys
                if (deletedPaths.isNotEmpty()) {
                    logger.info { "Deleted files in ${directory.absolutePathString()}: $deletedPaths" }
                    return@onChange deletedPaths.associateWith { DeletedSourceFile } + snapshot
                }

                // Exclude deleted files so they count as being added back
                val addedPaths = snapshot.keys - files.withoutDeletes().keys
                if (addedPaths.isNotEmpty()) {
                    logger.info { "Added files in ${directory.absolutePathString()}: $addedPaths" }
                    return@onChange files + snapshot
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
                    logger.info { "Timestamp changed in ${directory.absolutePathString()}: $modifiedFiles" }
                    return@onChange files + snapshot
                }

                error("Received a file system event but no changes were detected")
            })
        }
    }

    internal fun close() {
        thread.interrupt()
    }
}

internal fun SourceDirectory(directory: Path, listener: SourceDirectoryListener): SourceDirectory {
    return SourceDirectory(directory, directory.takeSnapshot(), listener)
}

private fun Path.takeSnapshot(): SourceFiles = walkFiles().associate { (it, attrs) ->
    it.relativeTo(this).pathString to SourceFile(attrs.lastModifiedTime().toInstant())
}.let(::SourceFiles)
