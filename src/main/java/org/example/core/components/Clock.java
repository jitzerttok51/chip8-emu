package org.example.core.components;

import java.util.concurrent.*;

public class Clock {

    private final int refreshRate;
    private Runnable task = ()->{};

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> future;

    public Clock(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public void start() {
        long cycleLength = Math.round((1.0f / refreshRate) * 1e9);
        future = executor.scheduleAtFixedRate(task, 0, cycleLength, TimeUnit.NANOSECONDS);
    }

    public void startAndWait() {
        start();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws InterruptedException {
        this.future.cancel(true);
        this.executor.shutdownNow();
        this.executor.awaitTermination(10, TimeUnit.MILLISECONDS);
    }
}
