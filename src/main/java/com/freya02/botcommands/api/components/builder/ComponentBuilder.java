package com.freya02.botcommands.api.components.builder;

import java.util.concurrent.TimeUnit;

public interface ComponentBuilder<T extends ComponentBuilder<T>> {
	/**
	 * Makes this component usable only once<br>
	 * This means once it is clicked if all the checks are valid and thus the handler has been executed, this won't be usable anymore
	 *
	 * @return This component builder for chaining purposes
	 */
	T oneUse();

	/**
	 * Makes this component usable only by the specified user<br>
	 * This means the component an only be interacted with by this user
	 *
	 * @param ownerId The ID of only user allowed to interact with this component
	 *                <br>Passing 0 means everyone can use this component, you can thus skip this call
	 * @return This component builder for chaining purposes
	 */
	T ownerId(long ownerId);

	/**
	 * Makes this component expire after the specified timeout<br>
	 * Once the component expires it should be removed from the component manager
	 *
	 * @return This component builder for chaining purposes
	 */
	T timeout(long timeout, TimeUnit timeoutUnit);

	boolean isOneUse();

	long getOwnerId();

	long getTimeout();
}
