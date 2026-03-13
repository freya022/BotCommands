package io.github.freya022.botcommands.internal.core.hooks

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.events.BGenericEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import java.util.*

private val logger = KotlinLogging.logger { }

@BService
internal class EventTreeService internal constructor() {
    private val map: Map<Class<*>, List<Class<*>>> = ClassGraph()
        .acceptPackages(GenericEvent::class.java.packageName, BGenericEvent::class.java.packageName)
        .disableRuntimeInvisibleAnnotations()
        .disableModuleScanning()
        .enableClassInfo()
        .scan().use { scanResult ->
            (scanResult.getClassesImplementing(GenericEvent::class.java) + scanResult.getClassesImplementing(BGenericEvent::class.java)).associate { info ->
                info.loadClass() to Collections.unmodifiableList(info.subclasses.map { subclassInfo -> subclassInfo.loadClass() })
            }
        }

    internal fun getSubclasses(clazz: Class<*>): List<Class<*>> = map[clazz] ?: emptyList<Class<*>>().also {
        logger.warn { "Unknown event type: ${clazz.name}" }
    }
}
