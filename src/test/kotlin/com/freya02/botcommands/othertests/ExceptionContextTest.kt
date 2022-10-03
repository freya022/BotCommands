package com.freya02.botcommands.othertests

import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ExceptionContext
import com.freya02.botcommands.internal.core.ExceptionContextInfo
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.events.CoroutineEventManager

suspend fun main() {
    val context = BContextImpl(BConfig().apply { packages += "com.freya02.botcommands.oldtests" }, CoroutineEventManager())
    val contextBlock: ExceptionContextInfo.() -> Unit = {
        logMessage = { "ok" }
        dispatchMessage = { "ok" }
        postRun = { println("oké") }
    }

    ExceptionContext.exceptionContext(context, "Test", contextBlock) {
        println("do sth")

        exceptionContext("Test 2") {
            throwUser("throw")
        }

        overrideHandler(contextBlock) {
            print("do sth 2")
        }
    }
}