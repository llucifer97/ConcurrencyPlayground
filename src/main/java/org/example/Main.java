package org.example;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
//
//        SharedQueue<Integer> queue = new SharedQueue<>(3);
//
//        Thread producer = new Thread(() -> {
//            for (int i = 1; i <= 10; i++) {
//                try {
//                    queue.put(i);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }, "Producer");
//
//        Thread consumer = new Thread(() -> {
//            for (int i = 0; i < 10; i++) {
//                try {
//                    queue.get();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }, "Consumer");
//
//        producer.start();
//        consumer.start();
//
//        producer.join();
//        consumer.join();

        // --- FixedThreadPool demo ---
//        System.out.println("\n--- FixedThreadPool demo ---");
//        FixedThreadPool pool = new FixedThreadPool(3, 10);
//
//        for (int i = 1; i <= 10; i++) {
//            int taskId = i;
//            pool.submit(() -> {
//                System.out.println(Thread.currentThread().getName() + " running task " + taskId);
//            });
//        }
//
//        Thread.sleep(1000); // let tasks finish
//        pool.shutdown();
//        pool.awaitTermination();
//
//        System.out.println("Done");

        // --- FlashSale (Semaphore) demo ---
//        AtomicInteger sold = new AtomicInteger(0);
//        FlashSale sale = new FlashSale(100);
//        Thread[] buyers = new Thread[1000];
//        for (int i = 0; i < 1000; i++) {
//            buyers[i] = new Thread(() -> { if (sale.buy()) sold.incrementAndGet(); });
//            buyers[i].start();
//        }
//        for (Thread b : buyers) b.join();
//        System.out.println("sold = " + sold.get());

        // --- FlashSale (AtomicInteger) demo ---
//        AtomicInteger sold = new AtomicInteger(0);
//        FlashSaleAtomic sale = new FlashSaleAtomic(100);
//
//        Thread[] buyers = new Thread[1000];
//        for (int i = 0; i < 1000; i++) {
//            buyers[i] = new Thread(() -> { if (sale.buy()) sold.incrementAndGet(); });
//            buyers[i].start();
//        }
//        for (Thread b : buyers) b.join();
//        System.out.println("sold = " + sold.get());

        // --- FlashSale Reservation demo ---
        System.out.println("--- FlashSale Reservation demo ---");
        int N = 100;
        int totalBuyers = 1000;
        FlashSaleReservation sale = new FlashSaleReservation(N);
        AtomicInteger released = new AtomicInteger(0);

        Thread[] buyers = new Thread[totalBuyers];
        for (int i = 0; i < totalBuyers; i++) {
            buyers[i] = new Thread(() -> {
                String token = sale.reserve();
                if (token == null) return; // sold out

                // simulate ~30% payment failure
                if (Math.random() < 0.3) {
                    sale.release(token);
                    released.incrementAndGet();
                } else {
                    sale.confirm(token);
                }

                // try double-release / double-confirm — should be rejected
                sale.release(token);
                sale.confirm(token);
            }, "Buyer-" + (i + 1));
            buyers[i].start();
        }
        for (Thread b : buyers) b.join();

        System.out.println("\n=== Results ===");
        System.out.println("Confirmed: " + sale.getConfirmedCount());
        System.out.println("Released:  " + released.get());
        System.out.println("Stock left: " + sale.getStock());
        System.out.println("confirmed + released + remaining stock = "
                + (sale.getConfirmedCount() + released.get() + sale.getStock()));

        assert sale.getConfirmedCount() <= N : "OVERSOLD!";
        assert sale.getConfirmedCount() + released.get() + sale.getStock() == N
                : "Units leaked! Total doesn't add up to " + N;
        System.out.println("All assertions passed.");

        // --- ReentrantMutex demo ---
        System.out.println("\n--- ReentrantMutex demo ---");
        ReentrantMutex mutex = new ReentrantMutex();
        int[] counter = {0}; // shared mutable state

        // 1) Mutual exclusion: 10 threads each increment counter 1000 times
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    try {
                        mutex.lock();
                        counter[0]++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        mutex.unlock();
                    }
                }
            }, "Worker-" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("Counter = " + counter[0] + " (expected 10000)");
        assert counter[0] == 10000 : "Race condition! counter = " + counter[0];

        // 2) Reentrancy: same thread locks twice without deadlocking
        mutex.lock();
        System.out.println("First lock, holdCount = " + mutex.getHoldCount());
        mutex.lock();
        System.out.println("Second lock (reentrant), holdCount = " + mutex.getHoldCount());
        mutex.unlock();
        System.out.println("One unlock, holdCount = " + mutex.getHoldCount());
        mutex.unlock();
        System.out.println("Fully unlocked, holdCount = " + mutex.getHoldCount());

        // 3) tryLock: non-blocking attempt while another thread holds it
        mutex.lock();
        Thread tryThread = new Thread(() -> {
            boolean got = mutex.tryLock();
            System.out.println("tryLock from other thread: " + got + " (expected false)");
        }, "TryLocker");
        tryThread.start();
        tryThread.join();
        mutex.unlock();

        // 4) tryLock succeeds when free
        boolean got = mutex.tryLock();
        System.out.println("tryLock when free: " + got + " (expected true)");
        mutex.unlock();

        System.out.println("All ReentrantMutex tests passed.");

    }
}