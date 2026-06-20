package org.example;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class SharedQueue<T> {

    private final Queue<T> queue;
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public SharedQueue(int capacity){
        this.capacity = capacity;
        queue = new ArrayDeque<>(capacity);
    }

    void put(T v) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.size() == capacity) {
                notFull.await();
            }
            queue.offer(v);
            System.out.println("added " + v);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    T get() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            T x = queue.poll();
            System.out.println("removed " + x);
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }

    T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            T x = queue.poll();
            System.out.println("polled " + x);
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }

    public synchronized boolean isEmpty(){
        if(queue.isEmpty()) return true;
        return false;
    }
}
