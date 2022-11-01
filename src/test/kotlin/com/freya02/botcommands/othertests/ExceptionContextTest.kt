package com.freya02.botcommands.othertests

import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ExceptionContext
import com.freya02.botcommands.internal.core.ExceptionContextInfo
import dev.minn.jda.ktx.events.CoroutineEventManager
import okio.IOException

suspend fun main() {
    val context = BContextImpl(BConfig().apply { packages += "com.freya02.botcommands.oldtests" }, CoroutineEventManager())
    val contextBlock: ExceptionContextInfo.() -> Unit = {
        logMessage = { "Generic log message" }
        dispatchMessage = { "Generic discord log message" }
        postRun = { println("I ran after an exception") }
    }

    ExceptionContext(context, contextBlock).runContext({ "Receiving a SlashCommandInteractionEvent" }) {
        println("do sth")
//        throw AssertionError("throw")

        withNewExceptionContext(contextBlock).runContext({ "Retrieving caller" }) {
            withExceptionContext("Awaiting network call") {
                throw IOException()
            }
        }

        overrideHandler(contextBlock) {
            print("do sth 2")
        }

        throw AssertionError("catch this")
    }
}