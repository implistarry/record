package com.impl.recoder.recordmp3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ExecutorManager
 * Description:
 */
public class ExecutorManager {

    private ExecutorService executorService;

    private ExecutorManager() {

    }

    public static ExecutorManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
        }
        return executorService;
    }
}
