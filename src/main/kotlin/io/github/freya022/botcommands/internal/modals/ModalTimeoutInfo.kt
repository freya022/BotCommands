package io.github.freya022.botcommands.internal.modals

import kotlin.time.Duration

internal class ModalTimeoutInfo internal constructor(val timeout: Duration, val onTimeout: suspend () -> Unit)