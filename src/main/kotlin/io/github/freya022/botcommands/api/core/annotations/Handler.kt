package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler

/**
 * Enables this class to be scanned for one or more handler.<br>
 * This is a specialization of [@BService][BService] for handlers.
 *
 * A warning will be logged if this class does not have any handlers,
 * i.e., methods that declare handlers with annotations.
 *
 * @see BService @BService
 *
 * @see JDAButtonListener @JDAButtonListener
 * @see JDASelectMenuListener @JDASelectMenuListener
 * @see AutocompleteHandler @AutocompleteHandler
 * @see ModalHandler @ModalHandler
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY) //Read by ClassGraph
annotation class Handler