package com.freya02.botcommands.api.annotations

/**
 * Marks a function as one which declares commands, you can make your slash commands, text commands, component listeners, etc... in this function
 *
 * **The function may be called more than once**, for example, if the bot needs to update its commands, or if it joins a guild
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Declaration //TODO rename to AppDeclaration, update docs