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

	public SmartCache(V defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void reset() {
		cache.clear();
	}

	public Optional<V> get(K key) {
		return Optional.ofNullable(cache.get(key));
	}

	public V getOrDefault(K key) {
		return cache.getOrDefault(key, defaultValue());
	}

	public void put(K key, V value) {
		Validate.notNull(key, "Key must not be null!");
		Validate.notNull(value, "Value must not be null!");
		cache.put(key, value);
	}

	public boolean hasKey(K key) {
		return key != null && cache.containsKey(key);
	}

	public boolean hasValue(V value) {
		return value != null && cache.containsValue(value);
	}

	public int size() {
		return cache.size();
	}

	/**
	 * The value which will be returned if the cache does not contain the requested key.
	 *
	 * @return the default value
	 */
	public V defaultValue() {
		return defaultValue;
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
