package com.freya02.botcommands.api.modals

import java.util.concurrent.TimeUnit

data class ModalTimeoutInfo internal constructor(val timeout: Long, val unit: TimeUnit, val onTimeout: Runnable)