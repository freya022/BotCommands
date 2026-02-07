package dev.freya02.botcommands.restarter.internal.sources

internal interface SourceDirectoriesListener {
    fun onChange(command: () -> Unit)

    fun onCancel()
}
