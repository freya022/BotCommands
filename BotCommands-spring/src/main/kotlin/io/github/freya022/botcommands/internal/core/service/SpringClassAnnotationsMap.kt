package io.github.freya022.botcommands.internal.core.service

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
@DependsOn("springBotCommandsBootstrap") // Forces reflection metadata to be scanned first
internal class SpringClassAnnotationsMap(
    private val context: ApplicationContext
) : ClassAnnotationsMap {
    override fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>? {
        val beansWithAnnotation = context.getBeansWithAnnotation(clazz.java)
        if (beansWithAnnotation.isEmpty()) return null

        return beansWithAnnotation.keys.mapTo(hashSetOf()) { context.getType(it)!!.kotlin }
    }
}