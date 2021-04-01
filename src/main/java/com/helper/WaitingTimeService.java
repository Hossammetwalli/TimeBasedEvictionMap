package com.helper;

public interface WaitingTimeService {
	public WaitingTimeService fixedWaitingService = new WaitingTimeService() {
		public void doWait(long ms) throws InterruptedException {
			WaitingTimeService.class.wait(ms);
		}

		public void doNotify() {
			WaitingTimeService.class.notifyAll();
		}
	};

	void doWait(long ms) throws InterruptedException;

	void doNotify();
}
