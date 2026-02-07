package dev.freya02.botcommands.restarter.internal.sources

import java.nio.file.Path

internal class SourceDirectories internal constructor() {
    private val directories: MutableMap<Path, SourceDirectory> = hashMapOf()

    internal fun getFile(path: String): ISourceFile? {
        return directories.firstNotNullOfOrNull { it.value.files[path] }
    }

    internal fun setSource(source: SourceDirectory) {
        directories[source.directory] = source
    }

    internal fun replaceSource(key: Path, directory: SourceDirectory) {
        check(key in directories)

        directories[key] = directory
    }

    internal fun close() {
        directories.values.forEach { it.close() }
    }
}

internal fun SourceDirectories(directories: List<Path>, listener: SourceDirectoriesListener): SourceDirectories {
    val sourceDirectories = SourceDirectories()

    fun onSourceDirectoryUpdate(directory: Path, sourceFilesFactory: () -> SourceFiles) {
        // The command is called when restarting
        // so we don't make snapshots before all changes went through
        listener.onChange(command = {
            val newSourceDirectory =
                SourceDirectory(
                    directory,
                    sourceFilesFactory(),
                    listener = { onSourceDirectoryUpdate(directory, it) }
                )
            sourceDirectories.replaceSource(directory, newSourceDirectory)
        })
    }

    directories.forEach { directory ->
        sourceDirectories.setSource(
            SourceDirectory(
                directory,
                listener = { onSourceDirectoryUpdate(directory, it) })
        )
    }

    return sourceDirectories
}
