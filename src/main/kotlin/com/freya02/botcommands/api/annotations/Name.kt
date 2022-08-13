package com.freya02.botcommands.api.annotations

/**
 * @param name This is the name used to display the option on Discord
 * @param declaredName This is the name used to make the association between the parameter and the declaration
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("For removal, use the appropriate parameters in SlashCommandBuilder#option instead", level = DeprecationLevel.ERROR)
annotation class Name(val name: String = "", val declaredName: String = "")