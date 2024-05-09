package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout

internal class EphemeralTimeout(
    val handler: suspend () -> Unit
) : ComponentTimeout