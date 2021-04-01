package com.helper;

public class ExpiryEntry<K> {

	private long expiryTime;
	private K key;

	public ExpiryEntry(long expiryTime, K key) {
		this.expiryTime = expiryTime;
		this.key = key;
	}

	public Long expiryTime() {
		return expiryTime;
	}

	public K key() {
		return key;
	}

}
