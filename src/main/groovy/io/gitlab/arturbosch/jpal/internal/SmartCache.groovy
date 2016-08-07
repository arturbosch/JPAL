package io.gitlab.arturbosch.jpal.internal

import groovy.transform.CompileStatic

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Artur
 */
@CompileStatic
public class SmartCache<K, V> {

	private final Map<K, V> cache = new ConcurrentHashMap<>();

	private final V defaultValue;

	public SmartCache() {
		this.defaultValue = null;
	}

	public void reset() {
		cache.clear();
	}

	public Optional<V> get(K key) {
		return Optional.ofNullable(cache.get(key));
	}

	public void put(K key, V value) {
		Validate.notNull(key, "Key must not be null!");
		Validate.notNull(value, "Value must not be null!");
		cache.put(key, value);
	}

	/**
	 * Internal representation of this cache.
	 *
	 * @return Internal representation of this cache
	 */
	Map<K, V> getInternalCache() {
		return cache;
	}

}
