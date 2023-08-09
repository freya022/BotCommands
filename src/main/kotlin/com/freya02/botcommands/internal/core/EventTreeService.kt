package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.events.BEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.KClass

@BService
internal class EventTreeService internal constructor() {
    private val map: Map<KClass<*>, List<KClass<*>>> = ClassGraph()
        .acceptPackages(Event::class.java.packageName, BEvent::class.java.packageName)
        .disableRuntimeInvisibleAnnotations()
        .disableModuleScanning()
        .disableNestedJarScanning()
        .enableClassInfo()
        .scan().use { scanResult ->
            scanResult.allStandardClasses.associate { info ->
                info.loadClass().kotlin to Collections.unmodifiableList(info.subclasses.map { subclassInfo -> subclassInfo.loadClass().kotlin })
            }
        }

    internal fun getSubclasses(kClass: KClass<*>): List<KClass<*>> = map[kClass] ?: emptyList()
}