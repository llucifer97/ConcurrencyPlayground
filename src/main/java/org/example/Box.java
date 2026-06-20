package org.example;

class Box {
    private volatile int value;
    private volatile boolean full = false;   // your condition flag




    public synchronized void put(int v) throws InterruptedException {
        while (full) wait();      // can't put into a full box
        value = v;
        full = true;
        notifyAll();
    }

    public synchronized int get() throws InterruptedException {
        while (!full) wait();     // can't take from an empty box
        full = false;
        notifyAll();
        return value;
    }



}