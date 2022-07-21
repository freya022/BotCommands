package com.freya02.botcommands.api.annotations

/**
 * @param name This is the name used to display the option on Discord
 * @param declaredName This is the name used to make the association between the parameter and the declaration
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val name: String = "", val declaredName: String = "")