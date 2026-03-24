package com.example.stamboot.config.logging;

import com.mgnt.utils.TextUtils;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * Global Logback interceptor to filter all stack traces using MgntUtils.
 */

public class MgntStackTraceConverter extends ThrowableProxyConverter {
	
	private static boolean cutTheBS = true;

	@Override
	public String convert(ILoggingEvent event) {
		IThrowableProxy proxy = event.getThrowableProxy();
		if (proxy == null) {
			return "";
		}

		// Extract the raw java.lang.Throwable from Logback's proxy object
		if (proxy instanceof ThrowableProxy) {
			Throwable rawException = ((ThrowableProxy) proxy).getThrowable();
			//remove first new line and add new line at the end
			return TextUtils.getStacktrace(rawException, cutTheBS).substring(1) + "\n";
		}

		// Fallback: If the exception is serialized over a network,
		// fallback to standard Logback formatting.
		return super.convert(event);
	}

	public static synchronized boolean isCutTheBS() {
		return cutTheBS;
	}

	public static synchronized void setCutTheBS(boolean cutTheBs) {
		MgntStackTraceConverter.cutTheBS = cutTheBs;
	}
}
