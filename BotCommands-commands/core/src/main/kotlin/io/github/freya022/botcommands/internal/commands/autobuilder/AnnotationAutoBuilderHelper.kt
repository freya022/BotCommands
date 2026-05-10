package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.Filter as FilterAnnotation
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.flatMap
import io.github.freya022.botcommands.api.core.utils.flatMapTo
import net.dv8tion.jda.api.Permission
import java.util.EnumSet
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal interface AnnotationAutoBuilderHelper {

    fun getFilterTypes(func: KFunction<*>): List<KClass<out Filter>> {
        return func.findAllAnnotations<FilterAnnotation>().flatMap { it.classes }
    }

    fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        return func.findAllAnnotations<UserPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }

    fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        return func.findAllAnnotations<BotPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }
}
