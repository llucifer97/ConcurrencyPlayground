package org.example;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitingRoom {

    private final Semaphore slots;
    private final int maxWaiters;
    private final AtomicInteger waitingCount = new AtomicInteger(0);
    private final AtomicInteger insideCount = new AtomicInteger(0);

    public WaitingRoom(int maxSlots, int maxWaiters) {
        this.slots = new Semaphore(maxSlots, true); // fair — FIFO ordering
        this.maxWaiters = maxWaiters;
    }

    public void enter() throws InterruptedException {
        if (waitingCount.incrementAndGet() > maxWaiters) {
            waitingCount.decrementAndGet();
            throw new IllegalStateException(Thread.currentThread().getName() + " rejected — wait queue full");
        }
        try {
            slots.acquire();
        } finally {
            waitingCount.decrementAndGet();
        }
        insideCount.incrementAndGet();
        System.out.println(Thread.currentThread().getName() + " entered. Inside: " + insideCount.get());
    }

    public boolean enter(long timeoutMs) throws InterruptedException {
        if (waitingCount.incrementAndGet() > maxWaiters) {
            waitingCount.decrementAndGet();
            throw new IllegalStateException(Thread.currentThread().getName() + " rejected — wait queue full");
        }
        boolean acquired;
        try {
            acquired = slots.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } finally {
            waitingCount.decrementAndGet();
        }
        if (acquired) {
            insideCount.incrementAndGet();
            System.out.println(Thread.currentThread().getName() + " entered (timed). Inside: " + insideCount.get());
        } else {
            System.out.println(Thread.currentThread().getName() + " timed out waiting for a slot");
        }
        return acquired;
    }

    public void leave() {
        insideCount.decrementAndGet();
        slots.release();
        System.out.println(Thread.currentThread().getName() + " left. Inside: " + insideCount.get());
    }

    public int getInsideCount() {
        return insideCount.get();
    }

    public int getWaitingCount() {
        return waitingCount.get();
    }
}
