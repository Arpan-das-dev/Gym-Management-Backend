package com.gym.adminservice.Utils.CustomAnnotations.helper;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class RequestTimeLoggerAspect {
    private static final Logger log = LoggerFactory.getLogger(RequestTimeLoggerAspect.class);

    // Define the desired date/time format
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    /**
     * Intercepts any method annotated with @LogRequestTime.
     * @param joinPoint The object representing the execution of the method.
     * @return The result of the target method call.
     * @throws Throwable if the target method throws an exception.
     */
    @Around("@annotation(com.gym.adminservice.Utils.CustomAnnotations.Annotations.LogRequestTime)")
    public Object logRequestTime(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. Get the current request time (before proceeding)
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(FORMATTER);

        // 2. Extract method details
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // 3. Log the request time
        log.info("⏰⏰:: Request received for {}.{} at {}", className, methodName, formattedTime);

        // 4. Proceed with the target method execution

        // The logic for after method execution can go here if needed,
        // but for logging request time, we just need the 'before' part.

        return joinPoint.proceed();
    }
}