package com.example.stamboot.config.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.mgnt.utils.TextUtils;
import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight, non-JMH benchmark for {@link MgntStackTraceConverter}.
 *
 * Disabled by default — flip {@link Disabled} off (or run from the IDE) to get
 * wall-clock + per-thread allocation numbers comparing:
 *
 *   - default Logback {@link ThrowableProxyConverter} (baseline)
 *   - MgntUtils filtering=true  (production path)
 *   - MgntUtils filtering=false (full stacktrace through MgntUtils)
 *
 * Not JMH-grade. Single-thread, JIT-warmup-by-loop, no fork. Use the absolute
 * numbers as order-of-magnitude estimates and the *ratios* as the real signal.
 */
@Disabled("Manual benchmark — run from the IDE or remove @Disabled to execute.")
class MgntStackTraceConverterBenchmarkTest {

    private static final String RELEVANT_PREFIX = "com.example.stamboot.";
    private static final int WARMUP_ITERATIONS = 5_000;
    private static final int MEASURE_ITERATIONS = 50_000;

    @BeforeAll
    static void configureRelevantPackage() {
        TextUtils.setRelevantPackage(RELEVANT_PREFIX);
    }

    @Test
    @DisplayName("Benchmark: filtered vs unfiltered vs default Logback rendering")
    void benchmark() {
        Throwable exception = buildRealisticException();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMessage("benchmark");
        event.setThrowableProxy(new ThrowableProxy(exception));

        ThrowableProxyConverter baseline = new ThrowableProxyConverter();
        baseline.start();

        MgntStackTraceConverter mgntConverter = new MgntStackTraceConverter();
        mgntConverter.start();

        warmup(new Runnable() {
            @Override public void run() { baseline.convert(event); }
        });
        warmup(new Runnable() {
            @Override public void run() {
                MgntStackTraceConverter.setCutTheBS(true);
                mgntConverter.convert(event);
            }
        });
        warmup(new Runnable() {
            @Override public void run() {
                MgntStackTraceConverter.setCutTheBS(false);
                mgntConverter.convert(event);
            }
        });

        Result baselineResult = measure("Default Logback ThrowableProxyConverter",
                new java.util.function.Supplier<String>() {
                    @Override public String get() { return baseline.convert(event); }
                });

        MgntStackTraceConverter.setCutTheBS(true);
        Result filteredResult = measure("MgntUtils filtering = true",
                new java.util.function.Supplier<String>() {
                    @Override public String get() { return mgntConverter.convert(event); }
                });

        MgntStackTraceConverter.setCutTheBS(false);
        Result unfilteredResult = measure("MgntUtils filtering = false",
                new java.util.function.Supplier<String>() {
                    @Override public String get() { return mgntConverter.convert(event); }
                });

        baseline.stop();
        mgntConverter.stop();

        printSummary(baselineResult, filteredResult, unfilteredResult);

        assertThat(baselineResult.checksum)
                .as("baseline Logback converter must produce non-empty output")
                .isPositive();
        assertThat(filteredResult.checksum)
                .as("MgntUtils filtered path must produce non-empty output")
                .isPositive();
        assertThat(unfilteredResult.checksum)
                .as("MgntUtils unfiltered path must produce non-empty output")
                .isPositive();
        assertThat(filteredResult.checksum)
                .as("filtering must shorten the rendered stacktrace vs unfiltered")
                .isLessThan(unfilteredResult.checksum);
    }

    private static void warmup(Runnable invocation) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            invocation.run();
        }
    }

    private static Result measure(String label, java.util.function.Supplier<String> invocation) {
        ThreadMXBean threadBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        long threadId = Thread.currentThread().getId();
        long allocBefore = threadBean.getThreadAllocatedBytes(threadId);
        long startNanos = System.nanoTime();

        long checksum = 0L;
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            String result = invocation.get();
            checksum += result.length();
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        long allocBytes = threadBean.getThreadAllocatedBytes(threadId) - allocBefore;
        return new Result(label, MEASURE_ITERATIONS, elapsedNanos, allocBytes, checksum);
    }

    private static void printSummary(Result... results) {
        System.out.println();
        System.out.println("=== MgntStackTraceConverter benchmark ===");
        System.out.printf("Iterations per row: %,d (after %,d warmup)%n",
                MEASURE_ITERATIONS, WARMUP_ITERATIONS);
        System.out.println();
        System.out.printf("%-50s | %12s | %12s | %14s%n",
                "Path", "total ms", "us / call", "bytes / call");
        System.out.println("----------------------------------------------------------------------------------------------------");
        for (int i = 0; i < results.length; i++) {
            Result r = results[i];
            double totalMs = r.elapsedNanos / 1_000_000.0;
            double usPerCall = (r.elapsedNanos / 1_000.0) / r.iterations;
            double bytesPerCall = (double) r.allocBytes / r.iterations;
            System.out.printf("%-50s | %12.2f | %12.3f | %14.0f%n",
                    r.label, totalMs, usPerCall, bytesPerCall);
        }
        System.out.println();
    }

    /** Builds an exception whose stacktrace looks like a real prod stacktrace. */
    private static Throwable buildRealisticException() {
        StackTraceElement[] root = new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.persistence.DbConnector", "connect", "DbConnector.java", 91),
                new StackTraceElement("com.example.stamboot.persistence.PoolManager", "borrow", "PoolManager.java", 18),
                new StackTraceElement("org.hibernate.engine.jdbc.connections.internal.DriverConnectionCreator",
                        "makeConnection", "DriverConnectionCreator.java", 53),
                new StackTraceElement("org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl", "getLogicalConnection",
                        "JdbcCoordinatorImpl.java", 207),
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed",
                        "ReflectiveMethodInvocation.java", 186),
                new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor",
                        "intercept", "CglibAopProxy.java", 763),
                new StackTraceElement("jdk.internal.reflect.GeneratedMethodAccessor1234", "invoke", null, -1),
                new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 568),
                new StackTraceElement("org.apache.tomcat.util.threads.TaskThread$WrappingRunnable", "run",
                        "TaskThread.java", 61),
                new StackTraceElement("java.lang.Thread", "run", "Thread.java", 829),
        };
        RuntimeException rootEx = new RuntimeException("RootCause: db connection refused");
        rootEx.setStackTrace(root);

        StackTraceElement[] middle = new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.repository.PurchaseRepo", "load", "PurchaseRepo.java", 122),
                new StackTraceElement("com.example.stamboot.repository.GenericRepo", "findOrFail", "GenericRepo.java", 88),
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed",
                        "ReflectiveMethodInvocation.java", 186),
                new StackTraceElement("org.springframework.transaction.interceptor.TransactionInterceptor", "invoke",
                        "TransactionInterceptor.java", 119),
                new StackTraceElement("org.springframework.aop.framework.JdkDynamicAopProxy", "invoke",
                        "JdkDynamicAopProxy.java", 215),
                new StackTraceElement("com.sun.proxy.$Proxy213", "load", null, -1),
        };
        RuntimeException middleEx = new RuntimeException("MiddleException: failed to load purchase #1234", rootEx);
        middleEx.setStackTrace(middle);

        StackTraceElement[] outer = new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.flow.PurchaseFlow", "run", "PurchaseFlow.java", 33),
                new StackTraceElement("com.example.stamboot.flow.PurchaseController", "execute", "PurchaseController.java", 87),
                new StackTraceElement("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter",
                        "invokeHandlerMethod", "RequestMappingHandlerAdapter.java", 880),
                new StackTraceElement("org.springframework.web.servlet.DispatcherServlet", "doDispatch",
                        "DispatcherServlet.java", 1071),
                new StackTraceElement("org.springframework.web.servlet.FrameworkServlet", "service",
                        "FrameworkServlet.java", 909),
                new StackTraceElement("javax.servlet.http.HttpServlet", "service", "HttpServlet.java", 779),
                new StackTraceElement("org.apache.catalina.core.ApplicationFilterChain", "doFilter",
                        "ApplicationFilterChain.java", 162),
                new StackTraceElement("org.apache.catalina.core.StandardWrapperValve", "invoke",
                        "StandardWrapperValve.java", 197),
                new StackTraceElement("org.apache.catalina.core.StandardContextValve", "invoke",
                        "StandardContextValve.java", 97),
                new StackTraceElement("org.apache.catalina.core.StandardEngineValve", "invoke",
                        "StandardEngineValve.java", 78),
                new StackTraceElement("org.apache.tomcat.util.threads.TaskThread$WrappingRunnable", "run",
                        "TaskThread.java", 61),
                new StackTraceElement("java.lang.Thread", "run", "Thread.java", 829),
        };
        RuntimeException outerEx = new RuntimeException("OuterException: GET /purchases/1234 failed", middleEx);
        outerEx.setStackTrace(outer);
        return outerEx;
    }

    private static final class Result {
        final String label;
        final int iterations;
        final long elapsedNanos;
        final long allocBytes;
        final long checksum;

        Result(String label, int iterations, long elapsedNanos, long allocBytes, long checksum) {
            this.label = label;
            this.iterations = iterations;
            this.elapsedNanos = elapsedNanos;
            this.allocBytes = allocBytes;
            this.checksum = checksum;
        }
    }
}
