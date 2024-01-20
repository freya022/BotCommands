package io.github.freya022.botcommands.internal.modals

import java.util.concurrent.TimeUnit

internal class ModalTimeoutInfo internal constructor(val timeout: Long, val unit: TimeUnit, val onTimeout: Runnable)