
// Updated KafkaJobRunner.java
package com.ticketsystem.kafka.runner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketsystem.kafka.handler.EventEnvelope;

/**
 * A job runner for managing Kafka consumer tasks.
 * Provides thread management and task execution for Kafka event processing.
 */
@Component
public class KafkaJobRunner {

    private static final Logger log = LoggerFactory.getLogger(KafkaJobRunner.class);

    private ExecutorService executorService;
    private final StreamBridge streamBridge;

    @Value("${spring.cloud.stream.kafka.job.thread-pool-size:10}")
    private int threadPoolSize;

    @Value("${spring.cloud.stream.kafka.job.thread-name-prefix:kafka-job-}")
    private String threadNamePrefix;

    @Value("${spring.cloud.stream.kafka.job.completion-topic:job-completion}")
    private String jobCompletionTopic;

    @Value("${spring.cloud.stream.kafka.job.shutdown-timeout-seconds:60}")
    private long shutdownTimeoutSeconds;

    public KafkaJobRunner(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostConstruct
    public void initialize() {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, new KafkaThreadFactory(threadNamePrefix));
        log.info("Initialized KafkaJobRunner with thread pool size: {} and thread prefix: {}", threadPoolSize,
                threadNamePrefix);
    }

    /**
     * Submit a job to be executed asynchronously
     * 
     * @param jobId     unique identifier for the job
     * @param eventName name of the event being processed
     * @param task      the task to execute
     * @return CompletableFuture for async handling
     */
    public CompletableFuture<Void> submitJob(String jobId, String eventName, Runnable task) {
        if (executorService == null || executorService.isShutdown()) {
            log.error("Cannot submit job {} - executor service is not available", jobId);
            return CompletableFuture.failedFuture(new IllegalStateException("Executor service not available"));
        }

        log.debug("Submitting job {} for event {}", jobId, eventName);

        return CompletableFuture.runAsync(() -> {
            String currentThread = Thread.currentThread().getName();
            try {
                log.debug("Starting job {} for event {} on thread {}", jobId, eventName, currentThread);
                long startTime = System.currentTimeMillis();

                task.run();

                long duration = System.currentTimeMillis() - startTime;
                log.debug("Completed job {} for event {} in {}ms on thread {}", jobId, eventName, duration,
                        currentThread);

                // Send completion notification
                notifyJobCompletion(jobId, eventName, true, null, duration);
            } catch (Exception e) {
                log.error("Job {} for event {} failed on thread {} with error: {}", jobId, eventName, currentThread,
                        e.getMessage(), e);

                // Send failure notification
                notifyJobCompletion(jobId, eventName, false, e.getMessage(), -1);
                throw new RuntimeException("Job execution failed", e);
            }
        }, executorService);
    }

    /**
     * Submit a job for processing an EventEnvelope with JsonNode payload
     * 
     * @param event the Kafka event to process
     * @param key   the job identifier key
     * @param task  the task to execute with the event
     * @return CompletableFuture for async handling
     */
    public CompletableFuture<Void> submitEventJob(EventEnvelope<JsonNode> event, String key, Runnable task) {
        if (event == null) {
            log.error("Cannot submit event job with null event");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Event cannot be null"));
        }

        String eventName = event.getEventName();
        return submitJob(key, eventName, task);
    }

    /**
     * Submit multiple jobs and return when all complete
     * 
     * @param jobs array of job submissions
     * @return CompletableFuture that completes when all jobs finish
     */
    public CompletableFuture<Void> submitAllJobs(CompletableFuture<Void>... jobs) {
        return CompletableFuture.allOf(jobs);
    }

    /**
     * Get the current number of active threads in the pool
     */
    public int getActiveThreadCount() {
        if (executorService instanceof java.util.concurrent.ThreadPoolExecutor) {
            return ((java.util.concurrent.ThreadPoolExecutor) executorService).getActiveCount();
        }
        return -1;
    }

    /**
     * Check if the executor service is healthy and accepting tasks
     */
    public boolean isHealthy() {
        return executorService != null && !executorService.isShutdown() && !executorService.isTerminated();
    }

    /**
     * Send job completion notification to Kafka
     */
    private void notifyJobCompletion(String jobId, String eventName, boolean success, String errorMessage,
            long executionTimeMs) {
        try {
            JobCompletionEvent completionEvent = new JobCompletionEvent(
                    jobId,
                    eventName,
                    System.currentTimeMillis(),
                    success,
                    errorMessage,
                    executionTimeMs);

            boolean sent = streamBridge.send(jobCompletionTopic, completionEvent);
            if (sent) {
                log.debug("Job completion notification sent for job {}", jobId);
            } else {
                log.warn("Failed to send job completion notification for job {}", jobId);
            }
        } catch (Exception e) {
            log.error("Error sending job completion notification for job {}: {}", jobId, e.getMessage(), e);
        }
    }

    /**
     * Custom thread factory to provide meaningful thread names and proper thread
     * configuration
     */
    private static class KafkaThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final ThreadGroup group;

        KafkaThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            thread.setUncaughtExceptionHandler((t, e) -> {
                log.error("Uncaught exception in thread {}: {}", t.getName(), e.getMessage(), e);
            });

            return thread;
        }
    }

    /**
     * Job completion event POJO for Kafka notifications
     */
    public static class JobCompletionEvent {
        private String jobId;
        private String eventName;
        private long timestamp;
        private boolean success;
        private String errorMessage;
        private long executionTimeMs;

        public JobCompletionEvent() {
        }

        public JobCompletionEvent(String jobId, String eventName, long timestamp, boolean success,
                String errorMessage, long executionTimeMs) {
            this.jobId = jobId;
            this.eventName = eventName;
            this.timestamp = timestamp;
            this.success = success;
            this.errorMessage = errorMessage;
            this.executionTimeMs = executionTimeMs;
        }

        // Getters and setters
        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public void setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }

        @Override
        public String toString() {
            return "JobCompletionEvent{" +
                    "jobId='" + jobId + '\'' +
                    ", eventName='" + eventName + '\'' +
                    ", timestamp=" + timestamp +
                    ", success=" + success +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", executionTimeMs=" + executionTimeMs +
                    '}';
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executorService == null) {
            log.info("KafkaJobRunner already shutdown or not initialized");
            return;
        }

        log.info("Shutting down KafkaJobRunner with timeout of {} seconds", shutdownTimeoutSeconds);
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate within {} seconds, forcing shutdown", shutdownTimeoutSeconds);
                executorService.shutdownNow();

                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate even after forced shutdown");
                }
            } else {
                log.info("KafkaJobRunner shutdown completed successfully");
            }
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted, forcing immediate termination", e);
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}