package com.freya02.botcommands.pagination.transformer;

/**
 * Interface to transform pagination entries into strings
 *
 * @param <T> Type of the pagination entry
 */
public interface EntryTransformer<T> {
	String toString(T t);
}
