package com.expirymap.test;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;

import org.junit.Test;

import com.expirymap.TimeBasedEvictionMap;

public class ExpiringMapTest {
	private long now;

	/*
	 * @Test general acceptance method map with duration of 10 seconds(10000 ms)
	 * 
	 * @Test Wait element to be expired and then remove element when expiry time
	 * been reached.
	 * 
	 * @Test remove element when expiry time been exceeded.
	 * 
	 * @Test remove multiple element when expiry time been exceeded.
	 * 
	 * @Test put and get several elements.
	 * 
	 * @ Test remove element by the key.
	 * 
	 */

	@Test
	public void testInternallyEvictingEntriesInDesiredTime() throws InterruptedException {
		TimeBasedEvictionMap<Integer, String> map = new TimeBasedEvictionMap<>();

		int expiryTimeMs = 10000;
		int numEntries = 10;

		for (int i = 0; i < numEntries; i++) {
			map.put(i, "v" + i, expiryTimeMs);
		}

		assertThat(map.size(), is(numEntries));

		Thread.sleep(expiryTimeMs + 10);

		assertThat(map.size(), is(0));
	}

	@Test
	public void testWaitAndRemoveEntriesAtExpiryTime() throws InterruptedException {
		// Given
		TimeBasedEvictionMap<String, String> map = new TimeBasedEvictionMap<>(() -> now);
		now = 0;
		int expiresInMs = 10000;
		map.put("k1", "v1", expiresInMs);

		// When
		now += MILLISECONDS.toNanos(expiresInMs);
		// Wait for removing expired items
		waitForKeyToBeRemoved("k1", map);

		// Then
		assertThat(map.get("k1"), is(nullValue()));

	}

	@Test
	public void testWaitAndRemoveMultipleEntriesAtExpiryTime() throws InterruptedException {
		// Given
		TimeBasedEvictionMap<String, String> map = new TimeBasedEvictionMap<>(() -> now);
		now = 0;
		int expiresInMs = 10000;
		map.put("k1", "v1", expiresInMs);
		map.put("k2", "v2", expiresInMs);
		map.put("k3", "v3", expiresInMs);

		// When
		now += MILLISECONDS.toNanos(expiresInMs);
		// Wait for removing expired items
		waitForKeyToBeRemoved("k1", map);
		waitForKeyToBeRemoved("k2", map);
		waitForKeyToBeRemoved("k3", map);

		// Then
		assertThat(map.get("k1"), is(nullValue()));
		assertThat(map.get("k2"), is(nullValue()));
		assertThat(map.get("k3"), is(nullValue()));

	}

	@Test
	public void testExpireEntriesAfterExpiryTimes() throws InterruptedException {
		// Given
		TimeBasedEvictionMap<String, String> map = new TimeBasedEvictionMap<>(() -> now);
		now = 0;
		int expiresInMs = 10000;
		map.put("k1", "v1", expiresInMs);

		// When
		now += MILLISECONDS.toNanos(expiresInMs + 1000);

		// Wait for removing expired items
		waitForKeyToBeRemoved("k1", map);

		// Then
		assertThat(map.get("k1"), is(nullValue()));
	}

	@Test
	public void testPutAndGetValues() {
		// Given
		TimeBasedEvictionMap<String, String> map = new TimeBasedEvictionMap<>();

		// When
		map.put("k1", "v1", HOURS.toMillis(1));
		map.put("k2", "v2", HOURS.toMillis(1));

		// Then
		assertThat(map.get("k1"), is("v1"));
		assertThat(map.get("k2"), is("v2"));
	}

	@Test
	public void testRemoveEntries() {
		// Given
		TimeBasedEvictionMap<String, String> map = new TimeBasedEvictionMap<>();
		map.put("key1", "value1", Long.MAX_VALUE);

		// When
		map.remove("key1");

		// Then
		assertThat(map.get("key1"), is(nullValue()));
	}

	private void waitForKeyToBeRemoved(String key, TimeBasedEvictionMap<String, String> map)
			throws InterruptedException {
		int count = 0;
		while (map.get(key) != null) {
			Thread.sleep(1);
			if (count++ > 100)
				throw new RuntimeException("Key took more than 2s to be removed: " + key);
		}
	}
}
