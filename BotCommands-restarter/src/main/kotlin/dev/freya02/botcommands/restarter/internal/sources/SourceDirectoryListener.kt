package dev.freya02.botcommands.restarter.internal.sources

internal fun interface SourceDirectoryListener {
    fun onChange(sourcesFilesFactory: () -> SourceFiles)
}
