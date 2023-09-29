package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.events.BEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import io.github.classgraph.ClassGraph
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }

@BService
internal class EventTreeService internal constructor() {
    private val map: Map<KClass<*>, List<KClass<*>>> = ClassGraph()
        .acceptPackages(Event::class.java.packageName, BEvent::class.java.packageName)
        .disableRuntimeInvisibleAnnotations()
        .disableModuleScanning()
        .disableNestedJarScanning()
        .enableClassInfo()
        .scan().use { scanResult ->
            scanResult.allClasses.filter { it.isStandardClass || it.isInterface }.associate { info ->
                info.loadClass().kotlin to Collections.unmodifiableList(info.subclasses.map { subclassInfo -> subclassInfo.loadClass().kotlin })
            }
        }

    internal fun getSubclasses(kClass: KClass<*>): List<KClass<*>> = map[kClass] ?: emptyList<KClass<*>>().also {
        logger.warn("Unknown event type: ${kClass.jvmName}")
    }
}