package io.github.freya022.botcommands.api.core.config

@RequiresOptIn(message = "This config property needs to be understood well and only used during development", level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class DevConfig
