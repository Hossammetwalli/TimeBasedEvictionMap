package com.expirymap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.helper.Clock;
import com.helper.ExpiryEntry;
import com.helper.WaitingTimeService;

/**
 * Class responsible for the timely expiring logic.
 *
 * @param <K>
 */
public class TimeBasedEvictionService<K> {

	public void removeExpiredItems(Clock clock, WaitingTimeService waitService, BlockingQueue<ExpiryEntry<K>> queue,
			Map<K, ?> backingMap) throws InterruptedException {
		ExpiryEntry<?> head = queue.take();

		// Check to remove expired elements otherwise, wait until expiry

		if (head.expiryTime() <= clock.timeStamp()) {
			backingMap.remove(head.key());
		} else {
			waitForNextItemToExpire(clock, waitService, queue, head);
		}
	}

	private void waitForNextItemToExpire(Clock clock, WaitingTimeService waitService,
			BlockingQueue<ExpiryEntry<K>> queue, ExpiryEntry head) throws InterruptedException {
		long waitTime = head.expiryTime() - clock.timeStamp();
		queue.offer(head);
		synchronized (WaitingTimeService.class) {
			if (waitTime > 0 && queue.peek().key().equals(head.key())) // ensure head has not been replaced
				waitService.doWait(ms(waitTime));
		}
	}

	// Convert waiting time from NS to MS
	private long ms(long waitTime) {
		return (long) Math.floor(waitTime / 1000000);
	}
}
