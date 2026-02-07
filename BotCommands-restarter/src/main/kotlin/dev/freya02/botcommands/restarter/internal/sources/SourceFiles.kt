package dev.freya02.botcommands.restarter.internal.sources

internal class SourceFiles internal constructor(
    internal val files: Map<String, ISourceFile>,
) {

    val keys: Set<String> get() = files.keys

    internal operator fun get(path: String): ISourceFile? = files[path]

    internal fun withoutDeletes(): SourceFiles = SourceFiles(files.filterValues { it !is DeletedSourceFile })

    internal operator fun plus(other: SourceFiles): SourceFiles = SourceFiles(files + other.files)
}

internal operator fun Map<String, ISourceFile>.plus(other: SourceFiles): SourceFiles = SourceFiles(this + other.files)
