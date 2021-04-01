package com.expirymap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.helper.Clock;
import com.helper.EvictionMap;
import com.helper.ExpiryEntry;
import com.helper.WaitingTimeService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;



/**
 * Generic structure EvictionMap<K, V> that acts as key-value map with following
 * time-based eviction Thread to read and wait for expiry time and then remove
 * from the map. Priority blocking queue add the items sorted by expiry time so
 * the next entry to expire will be at the head.
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */

public class TimeBasedEvictionMap<K, V> implements EvictionMap<K, V> {

	private Map<K, V> backingMap = new ConcurrentHashMap<>();
	private PriorityBlockingQueue<ExpiryEntry<K>> queue = new PriorityBlockingQueue<>(10,
			(element1, element2) -> element1.expiryTime().compareTo(element2.expiryTime()));

	private Clock clock;
	private WaitingTimeService waitService;

	public TimeBasedEvictionMap() {
		this(System::nanoTime);
	}

	public TimeBasedEvictionMap(Clock clock) {
		this(clock, WaitingTimeService.fixedWaitingService);
	}

	public TimeBasedEvictionMap(Clock clock, WaitingTimeService waitService) {
		this.clock = clock;
		this.waitService = waitService;
		startExpiryService();
	}

	@Override
	public void put(K key, V value, long timeoutMs) {
		validate(timeoutMs);

		long expiryTime = clock.timeStamp() + MILLISECONDS.toNanos(timeoutMs);

		queue.add(new ExpiryEntry<>(expiryTime, key));

		notifyEvictionForPreviousEntry(expiryTime);

		backingMap.put(key, value);
	}

	@Override
	public synchronized V get(K key) {
		return backingMap.get(key);
	}

	@Override
	public synchronized void remove(K key) {
		backingMap.remove(key);
	}

	public int size() {
		return backingMap.size();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void startExpiryService() {
		Thread thread = new Thread(() -> {
			TimeBasedEvictionService service = new TimeBasedEvictionService<K>();
			while (true) {
				try {
					service.removeExpiredItems(clock, waitService, queue, backingMap);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void notifyEvictionForPreviousEntry(long expiryTime) {
		ExpiryEntry<K> head = queue.peek();
		if (head != null && expiryTime <= head.expiryTime()) {
			synchronized (WaitingTimeService.class) {
				waitService.doNotify();
			}
		}
	}

	private void validate(long timeoutMs) {
		if (timeoutMs < 0)
			throw new IllegalArgumentException("Timeout must be a positive value");
	}

}
