package com.gym.planService.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    // pdf bean less core cpu intensive work
    @Bean(name = "pdfExecutor")
    public Executor pdfExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // no-of always active core
        executor.setMaxPoolSize(8); // no of max thread core can activate
        executor.setQueueCapacity(50); // setting max queue capacity of the tread
        executor.setThreadNamePrefix("PDF-EXEC-"); // defining the name of the thread
        executor.setWaitForTasksToCompleteOnShutdown(true);  // don't shut down system while it's busy
        executor.setAwaitTerminationSeconds(20);  // max time system will wait to complete execution
        executor.initialize();

        return executor;
    }

    // I/O heavy more threads
    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(25);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("UPLOAD-EXEC-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }

    @Bean(name = "defaultTasks")
    public Executor defaultExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("DEFAULT-EXEC-");
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(20);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setQueueCapacity(200);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
