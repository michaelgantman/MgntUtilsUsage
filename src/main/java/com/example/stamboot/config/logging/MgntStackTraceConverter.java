package com.example.stamboot.config.logging;

import com.mgnt.utils.TextUtils;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.apache.commons.lang3.StringUtils;

/**
 * Custom throwable converter that replaces Logback's standard exception rendering
 * with MgntUtils-based stacktrace filtering via
 * {@link TextUtils#getStacktrace(Throwable, boolean)}. This class only implements
 * the {@link ThrowableProxyConverter} contract — the wiring is done in
 * {@code logback.xml} (see the comments there for details).
 *
 * <p>For details of the filtering algorithm itself and how to configure the
 * relevant-package prefix, see the MgntUtils Javadoc and README.
 *
 * <p>The defensive shape (try/catch(Throwable), null/empty guards, conditional
 * leading-newline strip) may look over-protective given the current library
 * contract; it is intentional. Logback's appender drops the entire log event if a
 * converter throws, and customer integrations should not rely on a third-party
 * library's current internal behavior. On any unusable input the converter falls
 * through to {@code super.convert(event)}, so filtering may be lost in those edge
 * cases but the log event itself is not.
 *
 * @see TextUtils#getStacktrace(Throwable, boolean)
 */
public class MgntStackTraceConverter extends ThrowableProxyConverter {
	
	private static volatile boolean cutTheBS = true;

	@Override
	public String convert(ILoggingEvent event) {
		IThrowableProxy proxy = event.getThrowableProxy();
		if (proxy == null) {
			return "";
		}

		if (proxy instanceof ThrowableProxy) {
			Throwable rawException = ((ThrowableProxy)proxy).getThrowable();
			if (rawException != null) {
				String filtered = safeGetStacktrace(rawException);
				if (StringUtils.isNotEmpty(filtered)) {
					// TextUtils.getStacktrace currently prepends a newline; strip it only if present
					// so a future library change cannot leak a leading blank line into the output.
					String body = filtered.startsWith("\n") ? filtered.substring(1) : filtered;
					return body + "\n";
				}
			}
		}

		// Fallback for serialized remote exceptions, a null raw Throwable, unexpected
		// library output, or a library failure — defer to standard Logback rendering so
		// nothing is ever lost.
		return super.convert(event);
	}

	/**
	 * Calls the MgntUtils filter under a Throwable-catching guard. An uncaught
	 * exception inside a Logback converter is suppressed at the appender level and
	 * the entire log event is dropped — so any failure (RuntimeException,
	 * StackOverflowError on a cyclic cause chain, LinkageError, etc.) MUST be
	 * contained here. Returning {@code null} causes the caller to fall through to
	 * {@code super.convert(event)}, which renders the standard Logback stacktrace
	 * (unfiltered, but never silently dropped).
	 */
	private String safeGetStacktrace(Throwable rawException) {
		try {
			return TextUtils.getStacktrace(rawException, cutTheBS);
		} catch (Throwable libraryFailure) {
			return null;
		}
	}

	public static synchronized boolean isCutTheBS() {
		return cutTheBS;
	}

	public static synchronized void setCutTheBS(boolean cutTheBs) {
		MgntStackTraceConverter.cutTheBS = cutTheBs;
	}
}
