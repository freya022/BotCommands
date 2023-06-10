package com.freya02.botcommands.api.core.annotations;

import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables this class to be scanned for one or more handler.
 * <br>This is a specialization of {@link BService} for handlers.
 *
 * <p>A warning will be logged if this class does not have any handlers,
 * i.e. methods that declare handlers with annotations.
 *
 * @see BService
 *
 * @see JDAButtonListener
 * @see JDASelectMenuListener
 * @see AutocompleteHandler
 * @see ModalHandler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) //Read by ClassGraph
public @interface Handler { }