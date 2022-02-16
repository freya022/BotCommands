package com.freya02.botcommands.test

import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.*
import kotlin.concurrent.thread

//I'm pretty sure this is not ideal for kotlin, but you're the kotlin programmer, not me
class MethodRunnerScope {
    companion object {
        @JvmStatic private val pool = Utils.createCommandPool {
            val thread = thread(start = false, isDaemon = false, name = "Test thread", block = { it.run() })

            thread.setUncaughtExceptionHandler { t, e ->
                Utils.printExceptionString("An unexpected exception happened in a coroutine thread '" + t.name + "':", e)
            }

            thread
        }

        @JvmStatic val dispatcher = pool.asCoroutineDispatcher()

        // Using a SupervisorJob allows coroutines to fail without cancelling all other jobs
        @JvmStatic private val supervisor = SupervisorJob()
        // Implement a logging exception handler for uncaught throws in launched jobs
        @JvmStatic private val handler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                println("Uncaught exception in coroutine")

                throwable.printStackTrace()
            }
        }

        // Create our coroutine scope, this will stop the entire bot if a job fails
        @JvmStatic private val context = dispatcher + supervisor + handler
        @JvmStatic val scope = CoroutineScope(context)
    }
}