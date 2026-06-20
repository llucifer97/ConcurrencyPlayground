package org.example;

public class ReentrantMutex {

    private Thread owner = null;
    private int holdCount = 0;

    public synchronized void lock() throws InterruptedException {
        while (owner != null && owner != Thread.currentThread()) {
            wait();
        }
        owner = Thread.currentThread();
        holdCount++;
    }

    public synchronized void unlock() {
        if (owner != Thread.currentThread()) {
            throw new IllegalMonitorStateException("Current thread does not hold the lock");
        }
        holdCount--;
        if (holdCount == 0) {
            owner = null;
            notifyAll();
        }
    }

    public synchronized boolean tryLock() {
        if (owner == null || owner == Thread.currentThread()) {
            owner = Thread.currentThread();
            holdCount++;
            return true;
        }
        return false;
    }

    public synchronized int getHoldCount() {
        return holdCount;
    }

    public synchronized boolean isHeldByCurrentThread() {
        return owner == Thread.currentThread();
    }
}
