package org.perf4j.helpers;

import org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender.GroupedTimingStatisticsHandler;
import org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender.Perf4jEventHandler;

import junit.framework.TestCase;

public class GenericAsyncCoalescingStatisticsAppenderTest extends TestCase {

	private GenericAsyncCoalescingStatisticsAppender appender;

	public void setUp() {
		appender = new GenericAsyncCoalescingStatisticsAppender();
	}

	public void testStartGroupedTimingStaticticsHandlerNull() {
		try {
			appender.start((GroupedTimingStatisticsHandler) null);
			fail();
		} catch (IllegalArgumentException success) {
			assertEquals("handler cannot be null", success.getMessage());
		}
	}

	public void testStartPerf4jEventHandlerNull() {
		try {
			appender.start((Perf4jEventHandler) null);
			fail();
		} catch (IllegalArgumentException success) {
			assertEquals("handler cannot be null", success.getMessage());
		}
	}

	public void testAppendWithoutCallingStart() {
		try {
			appender.append("my message");
			fail();
		} catch (IllegalStateException success) {

		}
	}
}
