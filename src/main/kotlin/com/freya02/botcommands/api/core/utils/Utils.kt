package com.freya02.botcommands.api.core.utils

import mu.KLogger
import mu.KotlinLogging
import mu.toKLogger
import org.slf4j.LoggerFactory

@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> KotlinLogging.logger(): KLogger =
    LoggerFactory.getLogger(T::class.java).toKLogger()