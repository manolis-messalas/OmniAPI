package com.messalas.omniapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Enables Spring's @Scheduled support and configures the task scheduler to use
 * Java 23 virtual threads, keeping scheduler overhead near zero for lightweight
 * housekeeping jobs such as idempotency-key TTL cleanup.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Task scheduler used by all @Scheduled methods in the application.
     * Named "taskScheduler" so Spring picks it up automatically when resolving
     * the scheduler for @Scheduled annotations.
     *
     * <p>Pool size of 2 caps concurrent scheduled tasks; virtual threads make the
     * pool threads cheap so the cap is a logical limit, not a resource concern.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("idempotency-cleanup-");
        // Java 23 virtual threads: each task runs on a new virtual thread,
        // avoiding platform-thread blocking costs for I/O-bound cleanup work.
        scheduler.setThreadFactory(
                Thread.ofVirtual().name("idempotency-cleanup-", 0).factory());
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
