package org.example;

import java.util.concurrent.Semaphore;

public class FlashSale {

    private final Semaphore permits;

    public FlashSale(int stock) {
        this.permits = new Semaphore(stock);
    }

    public boolean buy() {
        if (permits.tryAcquire()) {
            System.out.println(Thread.currentThread().getName() + " bought an item. Remaining: " + permits.availablePermits());
            return true;
        }
        System.out.println(Thread.currentThread().getName() + " sold out!");
        return false;
    }
}
