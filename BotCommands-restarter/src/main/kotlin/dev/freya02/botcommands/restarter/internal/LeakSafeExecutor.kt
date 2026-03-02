package dev.freya02.botcommands.restarter.internal

import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import kotlin.system.exitProcess

internal class LeakSafeExecutor internal constructor() {

    // As we can only use a Thread once, we put a single LeakSafeThread in a blocking queue,
    // then, when a code block runs, a LeakSafeThread is removed from the queue,
    // and the LeakSafeThread recreates a new one for the next code block.
    // We use a blocking queue to prevent trying to get a LeakSafeThread between the moment it was retrieved and when it'll be added back
    private val leakSafeThreads: BlockingDeque<LeakSafeThread> = LinkedBlockingDeque()

    init {
        leakSafeThreads += LeakSafeThread()
    }

    fun <V> callAndWait(callable: () -> V): V = getLeakSafeThread().callAndWait(callable)

    private fun getLeakSafeThread(): LeakSafeThread {
        return leakSafeThreads.takeFirst()
    }

    /**
     * Thread that is created early so not to retain the [dev.freya02.botcommands.restarter.internal.RestartClassLoader].
     */
    private inner class LeakSafeThread : Thread() {

        private var callable: (() -> Any?)? = null

        private var result: Any? = null

        init {
            isDaemon = false
        }

        @Suppress("UNCHECKED_CAST")
        fun <V> callAndWait(callable: () -> V): V {
            this.callable = callable
            start()
            try {
                join()
                return this.result as V
            } catch (ex: InterruptedException) {
                currentThread().interrupt()
                throw IllegalStateException(ex)
            }
        }

        override fun run() {
            try {
                this@LeakSafeExecutor.leakSafeThreads.put(LeakSafeThread())
                this.result = this.callable!!.invoke()
            } catch (ex: Exception) {
                ex.printStackTrace()
                exitProcess(1)
            }
        }
    }
}
