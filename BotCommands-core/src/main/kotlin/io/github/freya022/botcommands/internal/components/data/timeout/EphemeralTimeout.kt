package io.github.freya022.botcommands.internal.components.data.timeout

internal class EphemeralTimeout(
    val handler: suspend () -> Unit
) : ComponentTimeout