package io.github.freya022.botcommands.api.core

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.internal.utils.JDALogger
import org.slf4j.Logger
import java.lang.reflect.Modifier

/**
 * Utilities returning loggers.
 */
object Logging {
    private val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

    /**
     * Returns the [Logger] for the class which calls this method.
     */
    @JvmStatic
    fun getLogger(): Logger {
        return JDALogger.getLog(stackWalker.callerClass.unwrapKotlinClassName())
    }

    /**
     * Returns the [Logger] for the class of this object.
     *
     * This might be useful to get a logger which targets an implementation class instead of a superclass.
     */
    @JvmStatic
    fun getLogger(obj: Any): Logger {
        return JDALogger.getLog(obj.javaClass.unwrapKotlinClassName())
    }

    /**
     * Returns the [KLogger] for the class which calls this method.
     *
     * If the logger is in a `Companion` object, or in a closure, it will be unwrapped into its enclosing class.
     */
    @JvmSynthetic
    fun currentLogger(): KLogger {
        return stackWalker.callerClass.toUnwrappedLogger()
    }

    @JvmSynthetic
    internal fun Class<*>.toUnwrappedLogger(): KLogger {
        return KotlinLogging.logger(unwrapKotlinClassName())
    }
}

/**
 * Returns the [KLogger] for the class of this object.
 *
 * This might be useful to get a logger which targets an implementation class instead of a superclass.
 */
@JvmSynthetic
fun Any.objectLogger(): KLogger {
    return KotlinLogging.logger(javaClass.unwrapKotlinClassName())
}

private fun Class<*>.unwrapKotlinClassName() = unwrapCompanionClass(this).name.substringBefore('$')

private fun <T : Any> unwrapCompanionClass(clazz: Class<T>): Class<*> {
    val enclosingClass = clazz.enclosingClass ?: return clazz

    val hasCompanionField = enclosingClass.declaredFields.any { field ->
        field.name == clazz.simpleName &&
                Modifier.isStatic(field.modifiers) &&
                field.type == clazz
    }

    return when {
        hasCompanionField -> enclosingClass
        else -> clazz
    }
}