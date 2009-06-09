package org.perf4j.helpers;

import junit.framework.TestCase;

public class GenericAsyncCoalescingStatisticsAppenderTest extends TestCase {

    private GenericAsyncCoalescingStatisticsAppender appender;
    
    public void setUp() {
	appender = new GenericAsyncCoalescingStatisticsAppender();
    }
    
    public void testStartNull() {
	try {
	    appender.start(null);
	    fail();
	}catch(IllegalArgumentException success) {
	    assertEquals("handler cannot be null",success.getMessage());
	}
    }
    
    public void testAppendWithoutCallingStart() {
	try {
	    appender.append("my message");
	    fail();
	}catch(IllegalStateException success) {
	    
	}
    }
}
