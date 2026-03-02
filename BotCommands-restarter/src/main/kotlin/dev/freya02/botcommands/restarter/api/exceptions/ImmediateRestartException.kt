package dev.freya02.botcommands.restarter.api.exceptions

import java.lang.reflect.InvocationTargetException

/**
 * Exception thrown intentionally after enabling the hot restart feature.
 *
 * This exception must propagate to the main method, and must not be caught, if it is, you must rethrow it.
 */
class ImmediateRestartException internal constructor() : RuntimeException("Dummy exception to stop the execution of the first main thread") {

    internal companion object {
        @JvmSynthetic
        internal fun throwAndHandle(): Nothing {
            val currentThread = Thread.currentThread()
            currentThread.uncaughtExceptionHandler = ExpectedRestartExceptionHandler(currentThread.uncaughtExceptionHandler)
            throw ImmediateRestartException()
        }
    }

    private class ExpectedRestartExceptionHandler(private val delegate: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {

        override fun uncaughtException(t: Thread, e: Throwable) {
            if (e is ImmediateRestartException || (e is InvocationTargetException && e.targetException is ImmediateRestartException)) {
                return
            }

            if (delegate != null) {
                delegate.uncaughtException(t, e)
            } else {
                e.printStackTrace()
            }
        }
    }
}
