package io.tomahawkd.blockchain.application.utils;

import com.sun.net.httpserver.HttpServer;

import java.util.concurrent.*;

public enum ThreadManager {

    INSTANCE;

    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


    public void addNewTask(Runnable runnable) {
        addNewTask(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> Future<T> addNewTask(Callable<T> runnable) {
        executor.purge();
        return executor.submit(runnable);
    }

    public void close() {
        executor.shutdown();
        executor.shutdownNow();
    }

    public void addToExecutor(HttpServer server) {
        server.setExecutor(executor);
    }
}
