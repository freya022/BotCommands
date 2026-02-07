package dev.freya02.botcommands.restarter.internal

import java.lang.reflect.InvocationTargetException

class ImmediateRestartException internal constructor() : RuntimeException("Dummy exception to stop the execution of the first main thread") {

    internal companion object {
        internal fun throwAndHandle(): Nothing {
            val currentThread = Thread.currentThread()
            currentThread.uncaughtExceptionHandler = ExpectedReloadExceptionHandler(currentThread.uncaughtExceptionHandler)
            throw ImmediateRestartException()
        }
    }

    private class ExpectedReloadExceptionHandler(private val delegate: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {

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
