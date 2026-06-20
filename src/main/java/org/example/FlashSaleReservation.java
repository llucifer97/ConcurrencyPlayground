package org.example;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleReservation {

    private final int maxStock;
    private final AtomicInteger stock;
    private final Set<String> activeReservations = ConcurrentHashMap.newKeySet();
    private final AtomicInteger confirmed = new AtomicInteger(0);

    public FlashSaleReservation(int stock) {
        this.maxStock = stock;
        this.stock = new AtomicInteger(stock);
    }

    public String reserve() {
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                return null; // sold out
            }
            if (stock.compareAndSet(current, current - 1)) {
                String token = UUID.randomUUID().toString();
                activeReservations.add(token);
                System.out.println(Thread.currentThread().getName() + " reserved. Token: " + token.substring(0, 8) + " Remaining: " + (current - 1));
                return token;
            }
        }
    }

    public boolean confirm(String token) {
        if (token == null || !activeReservations.remove(token)) {
            System.out.println(Thread.currentThread().getName() + " confirm REJECTED — invalid/duplicate token");
            return false;
        }
        confirmed.incrementAndGet();
        System.out.println(Thread.currentThread().getName() + " confirmed. Token: " + token.substring(0, 8));
        return true;
    }

    public boolean release(String token) {
        if (token == null || !activeReservations.remove(token)) {
            System.out.println(Thread.currentThread().getName() + " release REJECTED — invalid/duplicate token");
            return false;
        }
        stock.incrementAndGet();
        System.out.println(Thread.currentThread().getName() + " released. Token: " + token.substring(0, 8));
        return true;
    }

    public int getConfirmedCount() {
        return confirmed.get();
    }

    public int getStock() {
        return stock.get();
    }

    public int getMaxStock() {
        return maxStock;
    }
}
