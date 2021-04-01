package com.helper;

public interface EvictionMap<K, V> {

	/**
	 * Add the key/value pair as a new entry. If the map previously contained a
	 * value associated with key, the old value is replaced by value.
	 * 
	 * @param key
	 * @param value
	 * @param timeoutMs
	 */
	void put(K key, V value, long timeoutMs);

	/**
	 * Get the value associated with the key if present; otherwise, return null.
	 *
	 * @param key
	 * @return
	 */

	V get(K key);

	/**
	 * Remove the entry associated with key, if any.
	 *
	 * @param key
	 */
	void remove(K key);

}