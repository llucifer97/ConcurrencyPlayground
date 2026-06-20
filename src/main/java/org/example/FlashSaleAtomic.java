package org.example;

import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleAtomic {

    private final AtomicInteger stock;

    public FlashSaleAtomic(int stock) {
        this.stock = new AtomicInteger(stock);
    }

    public boolean buy() {
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                System.out.println(Thread.currentThread().getName() + " sold out!");
                return false;
            }
            if (stock.compareAndSet(current, current - 1)) {
                System.out.println(Thread.currentThread().getName() + " bought an item. Remaining: " + (current - 1));
                return true;
            }
            // CAS failed — another thread grabbed one, retry
        }
    }
}
