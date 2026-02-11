package dev.freya02.botcommands.restarter.internal.sources

import java.time.Instant

internal sealed interface ISourceFile

internal class SourceFile(
    val lastModified: Instant,
) : ISourceFile

internal object DeletedSourceFile : ISourceFile
