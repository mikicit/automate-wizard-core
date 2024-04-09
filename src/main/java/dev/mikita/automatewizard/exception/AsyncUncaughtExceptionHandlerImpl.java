package dev.mikita.automatewizard.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull;
import java.lang.reflect.Method;

@Slf4j
public class AsyncUncaughtExceptionHandlerImpl implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(@NonNull Throwable ex, @NonNull Method method, @NonNull Object... params) {
        log.error("Exception message - " + ex.getMessage());
        log.error("Method name - " + method.getName());
        for (Object param : params) {
            log.error("Parameter value - " + param);
        }
    }
}
