package com.stars.util;

import java.lang.management.ManagementFactory;

public class DeadLock {

	public static void findDeadLock() {
		long[] threadIds = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
		if (threadIds != null && threadIds.length > 0) {
			// 发现死锁
			ThreadGroup tg = Thread.currentThread().getThreadGroup();
			while (tg.getParent() != null) {
				tg = tg.getParent();
			}
			// 查找死锁的线程
			int threadCount = tg.activeCount() + 1024;
			Thread[] threads = new Thread[threadCount];
			tg.enumerate(threads, true);
			for (Thread t : threads) {
				for (long tid : threadIds) {
					try {
						if (t.getId() == tid) {
							t.interrupt(); // 中断
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
