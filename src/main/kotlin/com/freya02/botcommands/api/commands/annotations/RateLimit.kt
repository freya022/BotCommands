package com.freya02.botcommands.api.commands.annotations

//TODO docs
//TODO reference annotation
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit {
    //TODO base bucket and spike bucket will be configurable using 2 *nested* annotations (for Java and components usage)
}