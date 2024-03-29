package com.freya02.botcommands.api.modals.annotations;

import com.freya02.botcommands.api.modals.Modals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that this parameter is supplied by the data sent in {@link Modals#create(String, String, Object...)}.
 * <br>The data supplied in the method above must be in the same order as the modal handler parameters, and the types must match.
 *
 * @see ModalHandler
 * @see ModalInput
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ModalData {}
