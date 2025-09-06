
// Updated KafkaJobRunner.java
package com.ticketsystem.kafka.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketsystem.kafka.handler.EventEnvelope;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Job runner for managing Kafka consumer tasks.
 * Provides thread management and task execution for Kafka event processing.
 */
@Component
public class KafkaJobRunner {

    private static final Logger log = LoggerFactory.getLogger(KafkaJobRunner.class);

    private final ExecutorService executorService;
    private final int maxThreads;
    private final long keepAliveTime;

    /**
     * Default constructor with reasonable defaults
     */
    public KafkaJobRunner() {
        this(10, 60L, TimeUnit.SECONDS);
    }

    /**
     * Constructor with custom thread pool settings
     */
    public KafkaJobRunner(int maxThreads, long keepAliveTime, TimeUnit timeUnit) {
        this.maxThreads = maxThreads;
        this.keepAliveTime = keepAliveTime;

        this.executorService = new ThreadPoolExecutor(
                2, // corePoolSize
                maxThreads, // maximumPoolSize
                keepAliveTime, // keepAliveTime
                timeUnit, // time unit
                new LinkedBlockingQueue<>(100), // work queue with capacity
                new ThreadFactory() {
                    private int counter = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "kafka-job-runner-" + counter++);
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );

        log.info("KafkaJobRunner initialized with maxThreads: {}, keepAliveTime: {} {}",
                maxThreads, keepAliveTime, timeUnit);
    }

    /**
     * Submit an event processing job
     */
    public <T> CompletableFuture<Void> submitEventJob(EventEnvelope<JsonNode> message, String messageKey,
            Runnable task) {
        log.debug("Submitting event job for message key: {}, event: {}", messageKey, message.getEventName());

        return CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                log.debug("Starting processing for event: {} with key: {}", message.getEventName(), messageKey);

                task.run();

                long processingTime = System.currentTimeMillis() - startTime;
                log.debug("Completed processing for event: {} with key: {} in {}ms",
                        message.getEventName(), messageKey, processingTime);

            } catch (Exception e) {
                log.error("Error processing event job for key: {}, event: {}",
                        messageKey, message.getEventName(), e);
                throw new RuntimeException("Event processing failed", e);
            }
        }, executorService);
    }

    /**
     * Submit a generic task
     */
    public CompletableFuture<Void> submitTask(Runnable task, String taskName) {
        log.debug("Submitting generic task: {}", taskName);

        return CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                log.debug("Starting task: {}", taskName);

                task.run();

                long processingTime = System.currentTimeMillis() - startTime;
                log.debug("Completed task: {} in {}ms", taskName, processingTime);

            } catch (Exception e) {
                log.error("Error executing task: {}", taskName, e);
                throw new RuntimeException("Task execution failed: " + taskName, e);
            }
        }, executorService);
    }

    /**
     * Submit a task with result
     */
    public <T> CompletableFuture<T> submitTaskWithResult(Callable<T> task, String taskName) {
        log.debug("Submitting task with result: {}", taskName);

        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                log.debug("Starting task with result: {}", taskName);

                T result = task.call();

                long processingTime = System.currentTimeMillis() - startTime;
                log.debug("Completed task with result: {} in {}ms", taskName, processingTime);

                return result;

            } catch (Exception e) {
                log.error("Error executing task with result: {}", taskName, e);
                throw new RuntimeException("Task execution failed: " + taskName, e);
            }
        }, executorService);
    }

    /**
     * Get thread pool statistics
     */
    public ThreadPoolStats getStats() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return new ThreadPoolStats(
                    tpe.getCorePoolSize(),
                    tpe.getMaximumPoolSize(),
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getTaskCount(),
                    tpe.getCompletedTaskCount(),
                    tpe.getQueue().size());
        }
        return new ThreadPoolStats(0, maxThreads, 0, 0, 0, 0, 0);
    }

    /**
     * Check if the executor is healthy
     */
    public boolean isHealthy() {
        return !executorService.isShutdown() && !executorService.isTerminated();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down KafkaJobRunner...");

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate gracefully, forcing shutdown");
                executorService.shutdownNow();

                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            log.warn("Shutdown interrupted, forcing shutdown");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("KafkaJobRunner shutdown completed");
    }

    /**
     * Thread pool statistics
     */
    public static class ThreadPoolStats {
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final int activeCount;
        private final int poolSize;
        private final long taskCount;
        private final long completedTaskCount;
        private final int queueSize;

        public ThreadPoolStats(int corePoolSize, int maximumPoolSize, int activeCount,
                int poolSize, long taskCount, long completedTaskCount, int queueSize) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.activeCount = activeCount;
            this.poolSize = poolSize;
            this.taskCount = taskCount;
            this.completedTaskCount = completedTaskCount;
            this.queueSize = queueSize;
        }

        // Getters
        public int getCorePoolSize() {
            return corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public long getTaskCount() {
            return taskCount;
        }

        public long getCompletedTaskCount() {
            return completedTaskCount;
        }

        public int getQueueSize() {
            return queueSize;
        }

        @Override
        public String toString() {
            return String.format(
                    "ThreadPoolStats{core=%d, max=%d, active=%d, pool=%d, tasks=%d, completed=%d, queue=%d}",
                    corePoolSize, maximumPoolSize, activeCount, poolSize, taskCount, completedTaskCount, queueSize);
        }
    }
}
