package io.github.freya022.botcommands.internal.components

import net.dv8tion.jda.internal.components.AbstractComponentImpl
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
internal annotation class CustomJDAComponent(val jdaImpl: KClass<out AbstractComponentImpl>)