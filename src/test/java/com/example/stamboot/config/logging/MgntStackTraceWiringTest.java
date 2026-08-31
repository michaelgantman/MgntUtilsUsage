package com.example.stamboot.config.logging;

import com.mgnt.utils.TextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end wiring smoke test for the MgntUtils stacktrace filter.
 *
 * The unit tests in {@link MgntStackTraceConverterTest} drive {@code convert()}
 * directly with hand-built {@link ch.qos.logback.classic.spi.LoggingEvent}s.
 * That proves the converter works in isolation, but does NOT prove that:
 *   - {@code logback.xml} actually references the converter by the
 *     correct fully-qualified class name (a typo would silently disable
 *     filtering in production while every unit test still passes),
 *   - the conversion rule for {@code %ex} is in fact wired to our class,
 *   - the full pipeline SLF4J → Logback → MgntStackTraceConverter → CONSOLE
 *     produces filtered output for a real exception.
 *
 * This test closes that gap with one round-trip:
 *   1. Bring up a minimal Spring Boot context so {@code LoggingApplicationListener}
 *      loads {@code logback.xml}.
 *   2. Log a hand-built exception (mixed relevant + framework frames).
 *   3. Capture stdout via {@link OutputCaptureExtension} and assert the output
 *      is filtered (relevant frame present, deep framework noise collapsed).
 *
 * The CONSOLE appender is the active path in {@code logback.xml} (the JSON
 * appender is commented out). Both appenders share the same converter class;
 * this test exercises the {@code %ex} conversion-word wiring.
 */
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = MgntStackTraceWiringTest.MinimalSpringConfig.class)
class MgntStackTraceWiringTest {

    private static final String RELEVANT_PREFIX = "com.example.stamboot.";
    private static final Logger LOG = LoggerFactory.getLogger(MgntStackTraceWiringTest.class);

    @BeforeAll
    static void configureRelevantPackage() {
        // @SpringBootTest bootstraps Spring directly from MinimalSpringConfig — it does
        // NOT execute StamBootApplication.main(), so TextUtils.setRelevantPackage(...)
        // is never invoked by production code in this test. We mirror what main() does
        // so this test exercises the same wiring as prod.
        TextUtils.setRelevantPackage(RELEVANT_PREFIX);
    }

    @Test
    @DisplayName("logback.xml end-to-end: %ex routes through MgntStackTraceConverter and filters output")
    void endToEnd_consoleAppender_appliesMgntFiltering(CapturedOutput output) {
        Throwable exception = buildExceptionWithMixedFrames();

        LOG.error("wiring-smoke-test marker", exception);

        String captured = output.getOut();

        assertThat(captured)
                .as("the log event must reach the CONSOLE appender")
                .contains("wiring-smoke-test marker");

        assertThat(captured)
                .as("relevant-prefix frame must survive filtering")
                .contains("com.example.stamboot.SomeService.doWork")
                .as("MgntUtils skip marker must appear — proves the filter actually ran")
                .contains("...")
                .as("deep framework-only frames must be collapsed, not printed verbatim")
                .doesNotContain("java.lang.Thread.run(Thread.java:829)")
                .doesNotContain("org.apache.tomcat.util.threads.TaskThread");
    }

    /**
     * Hand-built stacktrace with a mix of {@code com.example.stamboot.} frames
     * (must survive filtering) and framework frames (must be collapsed).
     * Identical in shape to the unit-test fixture so behaviour assertions stay
     * in lock-step.
     */
    private static Throwable buildExceptionWithMixedFrames() {
        RuntimeException e = new RuntimeException("synthetic wiring failure");
        e.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.SomeService", "doWork", "SomeService.java", 42),
                new StackTraceElement("com.example.stamboot.SomeController", "handle", "SomeController.java", 17),
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation",
                        "proceed", "ReflectiveMethodInvocation.java", 186),
                new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor",
                        "intercept", "CglibAopProxy.java", 763),
                new StackTraceElement("jdk.internal.reflect.GeneratedMethodAccessor1234",
                        "invoke", null, -1),
                new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 568),
                new StackTraceElement("org.apache.tomcat.util.threads.TaskThread$WrappingRunnable",
                        "run", "TaskThread.java", 61),
                new StackTraceElement("java.lang.Thread", "run", "Thread.java", 829),
        });
        return e;
    }

    /**
     * Bare-bones Spring Boot configuration. Annotated with
     * {@link SpringBootConfiguration} (required by {@code @SpringBootTest})
     * but deliberately NOT {@code @SpringBootApplication} /
     * {@code @EnableAutoConfiguration} so none of the application's
     * auto-configurations are triggered. Logback initialisation happens in
     * Spring Boot's startup pipeline before any auto-config runs.
     */
    @SpringBootConfiguration
    static class MinimalSpringConfig {
    }
}
