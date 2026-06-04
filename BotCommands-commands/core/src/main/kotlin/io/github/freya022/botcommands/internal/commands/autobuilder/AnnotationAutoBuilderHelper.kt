package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.annotations.Filter as FilterAnnotation
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.flatMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object AnnotationAutoBuilderHelper {

    fun getFilterTypes(func: KFunction<*>): List<KClass<out Filter>> {
        return func.findAllAnnotations<FilterAnnotation>().flatMap { it.classes }
    }
}
