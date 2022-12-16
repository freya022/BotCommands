package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.events.BEvent
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.events.Event
import java.util.*
import kotlin.reflect.KClass

internal class EventTreeService(context: BContext) {
    private val map: Map<KClass<*>, List<KClass<*>>>

    init {
        context.putService(this)

        val map = hashMapOf<KClass<*>, List<KClass<*>>>()

        ClassGraph()
            .acceptPackages(Event::class.java.packageName, BEvent::class.java.packageName)
            .disableRuntimeInvisibleAnnotations()
            .disableModuleScanning()
            .disableNestedJarScanning()
            .enableClassInfo()
            .scan().use {
                it.allStandardClasses.forEach { info ->
                    map[info.loadClass().kotlin] = info.subclasses.map { subclassInfo -> subclassInfo.loadClass().kotlin }
                }
            }

        this.map = Collections.unmodifiableMap(map)
    }

    fun getSubclasses(kClass: KClass<*>): List<KClass<*>> = map[kClass] ?: emptyList()
}