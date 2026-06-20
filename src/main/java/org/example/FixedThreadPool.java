package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class FixedThreadPool {

    private final SharedQueue<Runnable> queue;
    private final List<Thread> workers = new ArrayList<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    FixedThreadPool(int nThreads, int queueCapacity) {
        this.queue = new SharedQueue<>(queueCapacity);

        for (int i = 0; i < nThreads; i++) {
            Thread worker = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable task = queue.get();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "pool-worker-" + i);
            workers.add(worker);
            worker.start();
        }
    }

    void submit(Runnable task) throws InterruptedException {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool is shut down, cannot accept new tasks");
        }
        queue.put(task);
    }

    void shutdown() {
        isShutdown.set(true);
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }

    void awaitTermination() throws InterruptedException {
        for (Thread worker : workers) {
            worker.join();
        }
    }
}
