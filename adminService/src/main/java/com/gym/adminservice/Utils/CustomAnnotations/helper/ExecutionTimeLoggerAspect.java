package com.gym.adminservice.Utils.CustomAnnotations.helper;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeLoggerAspect {
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeLoggerAspect.class);

    /**
     * This is the advice method. It "weaves" the timing logic around the target method.
     * @param joinPoint The object representing the execution of the method.
     * @return The result of the target method call.
     * @throws Throwable if the target method throws an exception.
     */
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            // Execute the target method
            Object result = joinPoint.proceed();

            // The method execution is completed
            long executionTime = System.currentTimeMillis() - start;

            // Construct the log message
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            log.info("⌛⌛:: {}.{} completed in {} ms", className, methodName, executionTime);

            return result;
        } catch (Throwable t) {
            // Log if the method threw an exception (optional, but good practice)
            long executionTime = System.currentTimeMillis() - start;
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            log.error("❌❌:: {}.{} failed after {} ms with error: {}",
                    className, methodName, executionTime, t.getMessage());
            throw t;
        }
    }
}
