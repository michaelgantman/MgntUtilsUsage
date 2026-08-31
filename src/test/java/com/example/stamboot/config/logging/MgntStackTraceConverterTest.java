package com.example.stamboot.config.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Map;
import com.mgnt.utils.TextUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavioural tests for {@link MgntStackTraceConverter}.
 *
 * Drives convert() through real {@link Throwable} instances rather than mocking
 * MgntUtils — the goal is to lock in the externally-visible contract:
 *   - never throws inside the logging pipeline
 *   - never silently swallows a stacktrace
 *   - filters relevant-package frames in, framework noise out, when filtering is enabled
 *   - returns the full stacktrace when filtering is disabled
 *   - falls back to the parent ThrowableProxyConverter for non-ThrowableProxy inputs
 *
 * The library-throws branch IS exercised — see
 * {@code convert_libraryThrows_neverDropsLogEvent} — by a throwable whose
 * {@code printStackTrace(PrintStream)} explodes (that is what TextUtils calls).
 * That path, if regressed, would silently destroy entire log events at the
 * appender level.
 *
 * Self-configuring: the relevant-package prefix is set programmatically in
 * {@code @BeforeAll} via {@link TextUtils#setRelevantPackage(String)}. This test
 * does NOT depend on {@code -Dmgnt.relevant.package}. Production code does the
 * same thing in {@code StamBootApplication#main(...)}.
 */
class MgntStackTraceConverterTest {

    private static final String RELEVANT_PREFIX = "com.example.stamboot.";

    private MgntStackTraceConverter converter;
    private boolean originalCutTheBs;

    @BeforeAll
    static void configureRelevantPackage() {
        // Mirror what StamBootApplication.main() does in production. MgntUtils caches the
        // prefix on first use of TextUtils.getStacktrace(...), so we MUST set it before
        // any test invokes the converter.
        TextUtils.setRelevantPackage(RELEVANT_PREFIX);
    }

    @BeforeEach
    void setUp() {
        converter = new MgntStackTraceConverter();
        converter.start();
        originalCutTheBs = MgntStackTraceConverter.isCutTheBS();
    }

    @AfterEach
    void tearDown() {
        MgntStackTraceConverter.setCutTheBS(originalCutTheBs);
        converter.stop();
    }

    @Test
    @DisplayName("null IThrowableProxy returns empty string and never throws")
    void convert_nullThrowableProxy_returnsEmptyString() {
        LoggingEvent event = newLoggingEventWithoutThrowable();

        String rendered = converter.convert(event);

        assertThat(rendered).isEmpty();
    }

    @Test
    @DisplayName("filtering enabled: relevant frames preserved, framework noise collapsed (with one transition frame)")
    void convert_filteringEnabled_keepsRelevantFramesAndCollapsesFrameworkNoise() {
        MgntStackTraceConverter.setCutTheBS(true);
        Throwable exception = buildExceptionWithMixedFrames();
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);

        // MgntUtils contract:
        //   - every relevant-prefix frame survives
        //   - the first framework frame *after* a relevant section is kept as a transition frame
        //   - all subsequent framework frames in that run are collapsed to a single "..."
        //   - deeper framework-only frames (Tomcat, Thread.run) must not appear verbatim
        assertThat(rendered)
                .as("relevant-prefix frames must be preserved")
                .contains("com.example.stamboot.SomeService.doWork")
                .contains("com.example.stamboot.SomeController.handle")
                .as("framework noise must be collapsed to the MgntUtils skip marker")
                .contains("...")
                .as("framework frames deep into the noise tail must NOT survive verbatim")
                .doesNotContain("org.apache.tomcat.util.threads.TaskThread")
                .doesNotContain("java.lang.Thread.run")
                .as("rendered output must end with exactly one trailing newline")
                .endsWith("\n")
                .doesNotStartWith("\n\n");
        String unfiltered = TextUtils.getStacktrace(exception, false);
        assertThat(rendered.length()).isLessThan(unfiltered.length());
    }

    @Test
    @DisplayName("filtering disabled: full stacktrace, no skip markers, every frame survives")
    void convert_filteringDisabled_returnsFullStacktrace() {
        MgntStackTraceConverter.setCutTheBS(false);
        Throwable exception = buildExceptionWithMixedFrames();
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);

        assertThat(rendered)
                .contains("com.example.stamboot.SomeService.doWork")
                .contains("org.springframework.aop.framework.ReflectiveMethodInvocation")
                .contains("org.apache.tomcat.util.threads.TaskThread")
                // MgntUtils skip marker line is exactly "\t...", so its absence proves no filtering happened.
                .doesNotContain("\n\t...")
                .endsWith("\n");
    }

    @Test
    @DisplayName("Caused-by chain: every section header is preserved even when filtering")
    void convert_causedByChain_preservesAllSectionHeaders() {
        MgntStackTraceConverter.setCutTheBS(true);
        Throwable exception = buildExceptionWithCausedByChain();
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);

        assertThat(rendered)
                .contains("OuterRuntimeException")
                .contains("Caused by:")
                .contains("MiddleException")
                .contains("RootCauseException")
                .endsWith("\n");
    }

    @Test
    @DisplayName("non-ThrowableProxy (serialised remote) falls back to parent converter rendering")
    void convert_nonThrowableProxy_fallsBackToSuperConverter() {
        IThrowableProxy serialisedProxy = stubSerialisedThrowableProxy();
        ILoggingEvent event = newLoggingEventWith(serialisedProxy);

        String rendered = converter.convert(event);

        assertThat(rendered)
                .as("super.convert(...) must render *something* — never silently drop the stacktrace")
                .isNotEmpty()
                .contains("RemoteRuntimeException")
                .contains("remote.frame.RemoteWorker.execute");
    }

    @Test
    @DisplayName("library throws: converter never propagates the failure — log event survives via super.convert fallback")
    void convert_libraryThrows_neverDropsLogEvent() {
        // Critical safety contract: an uncaught exception inside a Logback converter
        // is suppressed at the appender level and the *entire log event* (message,
        // MDC, level) is silently dropped. TextUtils.getStacktrace calls
        // Throwable.printStackTrace(PrintStream); exploding there simulates a
        // library failure without a static-mock dependency (mockito-inline).
        MgntStackTraceConverter.setCutTheBS(true);
        Throwable exception = buildExceptionThatExplodesOnPrint(
                new RuntimeException("simulated MgntUtils failure"));
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);

        assertThat(rendered)
                .as("super.convert(...) fallback must keep the stacktrace alive")
                .isNotEmpty()
                .contains("synthetic failure")
                .contains("com.example.stamboot.SomeService.doWork");
    }

    @Test
    @DisplayName("library throws Error (cyclic chain → StackOverflowError): still contained, log event survives")
    void convert_libraryThrowsError_neverDropsLogEvent() {
        MgntStackTraceConverter.setCutTheBS(true);
        Throwable exception = buildExceptionThatExplodesOnPrint(
                new StackOverflowError("simulated cyclic-chain blow-up"));
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);

        assertThat(rendered)
                .as("Error from library must not propagate; super.convert renders unfiltered output")
                .isNotEmpty()
                .contains("synthetic failure");
    }

    @Test
    @DisplayName("setCutTheBS toggles state; getter reflects the change")
    void setCutTheBS_togglesState() {
        MgntStackTraceConverter.setCutTheBS(false);
        assertThat(MgntStackTraceConverter.isCutTheBS()).isFalse();

        MgntStackTraceConverter.setCutTheBS(true);
        assertThat(MgntStackTraceConverter.isCutTheBS()).isTrue();
    }

    @Test
    @DisplayName("rendered output never starts with the leading-newline that TextUtils prepends")
    void convert_neverEmitsLibraryLeadingNewline() {
        MgntStackTraceConverter.setCutTheBS(true);
        Throwable exception = buildExceptionWithMixedFrames();
        LoggingEvent event = newLoggingEventWith(exception);

        String rendered = converter.convert(event);
        String rawLibraryOutput = TextUtils.getStacktrace(exception, true);

        assertThat(rawLibraryOutput)
                .as("Library contract assumed by the converter: leading-\\n is currently present.")
                .startsWith("\n");
        assertThat(rendered)
                .as("Converter must strip that leading-\\n exactly once.")
                .doesNotStartWith("\n");
    }

    private static LoggingEvent newLoggingEventWithoutThrowable() {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMessage("no throwable here");
        return event;
    }

    private static LoggingEvent newLoggingEventWith(Throwable t) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMessage("boom");
        event.setThrowableProxy(new ThrowableProxy(t));
        return event;
    }

    /**
     * Hand-built ILoggingEvent so we can return a non-ThrowableProxy proxy —
     * {@link LoggingEvent#setThrowableProxy(ThrowableProxy)} is typed for the
     * concrete ThrowableProxy class and won't accept a plain IThrowableProxy.
     *
     * Implements only the Logback 1.2 {@link ILoggingEvent} surface (this project
     * is Java 8 / Spring Boot 2.6 / Logback 1.2).
     */
    private static ILoggingEvent newLoggingEventWith(final IThrowableProxy proxy) {
        return new ILoggingEvent() {
            @Override public String getThreadName() { return Thread.currentThread().getName(); }
            @Override public Level getLevel() { return Level.ERROR; }
            @Override public String getMessage() { return "boom"; }
            @Override public Object[] getArgumentArray() { return new Object[0]; }
            @Override public String getFormattedMessage() { return "boom"; }
            @Override public String getLoggerName() { return MgntStackTraceConverterTest.class.getName(); }
            @Override public LoggerContextVO getLoggerContextVO() { return null; }
            @Override public IThrowableProxy getThrowableProxy() { return proxy; }
            @Override public StackTraceElement[] getCallerData() { return new StackTraceElement[0]; }
            @Override public boolean hasCallerData() { return false; }
            @Override public Marker getMarker() { return null; }
            @Override public Map<String, String> getMDCPropertyMap() { return Collections.emptyMap(); }
            @Override public Map<String, String> getMdc() { return Collections.emptyMap(); }
            @Override public long getTimeStamp() { return System.currentTimeMillis(); }
            @Override public void prepareForDeferredProcessing() {
                // Intentionally empty: this stub event is constructed eagerly with
                // all fields set to constants, so there is nothing to capture for
                // deferred async-appender processing.
            }
        };
    }

    /**
     * Hand-builds a Throwable whose stacktrace contains a mix of:
     *   - com.example.stamboot. frames (must survive filtering)
     *   - Spring AOP / Tomcat / reflection frames (must be collapsed)
     */
    private static Throwable buildExceptionWithMixedFrames() {
        RuntimeException e = new RuntimeException("synthetic failure");
        e.setStackTrace(mixedFrames());
        return e;
    }

    /**
     * Same mixed frames as {@link #buildExceptionWithMixedFrames()}, but
     * {@code printStackTrace(PrintStream)} throws. TextUtils.getStacktrace
     * invokes that method, so this exercises the converter's catch(Throwable)
     * guard. Logback's {@link ThrowableProxy} captures frames via
     * {@code getStackTrace()} at construction time, so {@code super.convert}
     * still has a stacktrace to render.
     */
    private static Throwable buildExceptionThatExplodesOnPrint(final Throwable explosion) {
        RuntimeException e = new RuntimeException("synthetic failure") {
            @Override
            public void printStackTrace(PrintStream s) {
                if (explosion instanceof Error) {
                    throw (Error) explosion;
                }
                if (explosion instanceof RuntimeException) {
                    throw (RuntimeException) explosion;
                }
                throw new RuntimeException(explosion);
            }
        };
        e.setStackTrace(mixedFrames());
        return e;
    }

    private static StackTraceElement[] mixedFrames() {
        return new StackTraceElement[] {
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
        };
    }

    private static Throwable buildExceptionWithCausedByChain() {
        RuntimeException root = new RuntimeException("RootCauseException: db down");
        root.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.persistence.DbConnector",
                        "connect", "DbConnector.java", 91),
                new StackTraceElement("org.hibernate.engine.jdbc.connections.internal.DriverConnectionCreator",
                        "makeConnection", "DriverConnectionCreator.java", 53),
        });

        RuntimeException middle = new RuntimeException("MiddleException", root);
        middle.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.repository.PurchaseRepo",
                        "load", "PurchaseRepo.java", 122),
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation",
                        "proceed", "ReflectiveMethodInvocation.java", 186),
        });

        RuntimeException outer = new RuntimeException("OuterRuntimeException", middle);
        outer.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.example.stamboot.flow.PurchaseFlow",
                        "run", "PurchaseFlow.java", 33),
                new StackTraceElement("org.springframework.web.servlet.DispatcherServlet",
                        "doDispatch", "DispatcherServlet.java", 1071),
        });

        return outer;
    }

    /**
     * Mimics a deserialised remote-side throwable proxy: an IThrowableProxy that
     * is NOT a {@link ThrowableProxy}, has no underlying raw {@link Throwable},
     * and is the case the converter must hand back to its parent.
     */
    private static IThrowableProxy stubSerialisedThrowableProxy() {
        final StackTraceElementProxy[] frames = new StackTraceElementProxy[] {
                new StackTraceElementProxy(new StackTraceElement(
                        "remote.frame.RemoteWorker", "execute", "RemoteWorker.java", 7)),
        };
        return new IThrowableProxy() {
            @Override public String getMessage() { return "remote failure"; }
            @Override public String getClassName() { return "RemoteRuntimeException"; }
            @Override public StackTraceElementProxy[] getStackTraceElementProxyArray() { return frames; }
            @Override public int getCommonFrames() { return 0; }
            @Override public IThrowableProxy getCause() { return null; }
            @Override public IThrowableProxy[] getSuppressed() { return new IThrowableProxy[0]; }
        };
    }
}
