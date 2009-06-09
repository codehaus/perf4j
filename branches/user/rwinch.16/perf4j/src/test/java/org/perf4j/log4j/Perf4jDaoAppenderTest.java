/* Copyright (c) 2008-2009 HomeAway, Inc.
 * All rights reserved.  http://www.perf4j.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.perf4j.log4j;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.dao.Perf4jDao;

public class Perf4jDaoAppenderTest extends TestCase {

    private Perf4jDaoAppender appender;

    private Mockery mock;

    public void setUp() {
	mock = new Mockery();
	appender = new Perf4jDaoAppender();
	appender.setPerf4jDao(mock.mock(Perf4jDao.class));
    }

    public void tearDown() throws Exception {
	mock.assertIsSatisfied();
	super.tearDown();
    }

    // start append
    public void testAppendNullLoggingEvent() {
	try {
	    appender.append(null);
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals("loggingEvent cannot be null", success.getMessage());
	}
    }

    /**
     * should get no errors and not interact w/ the dao
     */
    public void testAppendMessageNull() {
	appender.append(createEvent(null));
    }

    /**
     * should get no errors and not interact w/ the dao
     */
    public void testAppendMessageString() {
	appender.append(createEvent("no errors here"));
    }

    public void testAppendMessageGroupedTimingStatistics() {
	final GroupedTimingStatistics gts = new GroupedTimingStatistics();
	mock.checking(new Expectations() {
	    {
		oneOf(appender.getPerf4jDao()).saveGroupedTimingStatistics(
			Collections.singleton(gts));
	    }
	});
	appender.append(createEvent(gts));
    }

    public void testAppendMessageStopWatch() {
	final StopWatch stopWatch = new StopWatch("tag", "message");
	stopWatch.stop();

	mock.checking(new Expectations() {
	    {
		oneOf(appender.getPerf4jDao()).saveStopWatches(
			Collections.singleton(stopWatch));
	    }
	});
	appender.append(createEvent(stopWatch));
    }

    // end append

    // start setPerf4jDao

    public void testSetPerf4jDaoNull() {
	try {
	    appender.setPerf4jDao(null);
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals("perf4jDao cannot be null", success.getMessage());
	}
    }

    // end setPerf4jDao

    // start setDriver
    public void testSetDriverClassNotFound() {
	try {
	    appender.setDriver("org.perf4j.log4j.MissingJdbcDriver");
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals(
		    "Could not load driver org.perf4j.log4j.MissingJdbcDriver",
		    success.getMessage());
	}
    }

    // end setDriver

    // start close
    public void testCloseEnsureConnectionClosed() throws Exception {
	final Connection connection = mock.mock(Connection.class);
	setField("connection", connection);

	mock.checking(new Expectations() {
	    {
		oneOf(connection).close();
	    }
	});
	appender.close();
    }

    public void testCloseNullConnection() {
	appender.activateOptions();
	appender.close();
    }

    public void testCloseNotActivated() {
	appender = new Perf4jDaoAppender();
	appender.close();
    }

    // end close

    // start activateOptions

    public void testActivateParametersNoParametersSet() {
	appender = new Perf4jDaoAppender();
	try {
	    appender.activateOptions();
	    fail("Expected Exception");
	} catch (RuntimeException success) {
	    assertEquals(
		    "Couldn't create a connection with the parameters databaseUrl=null, databaseUser=null, databasePassword=null",
		    success.getMessage());
	}
    }

    public void testActivateParametersPerf4jDaoSetAlready() {
	final Perf4jDao expected = appender.getPerf4jDao();
	appender.activateOptions();
	assertEquals(expected, appender.getPerf4jDao());
    }

    public void testActivateParameters() {
	appender = new Perf4jDaoAppender();
	assertNull(appender.getPerf4jDao());

	appender.setDatabasePassword("");
	appender.setDatabaseUrl("jdbc:h2:target/h2/perf4j");
	appender.setDriver("org.h2.Driver");
	appender.setDatabaseUser("sa");

	appender.activateOptions();
	assertNotNull(appender.getPerf4jDao());
    }

    // end activateOptions

    // -- helper methods --

    private LoggingEvent createEvent(Object message) {
	return new LoggingEvent(Logger.class.getName(), Logger
		.getLogger(StopWatch.DEFAULT_LOGGER_NAME), System
		.currentTimeMillis(), Level.INFO, message, null);
    }

    private void setField(String name, Object value) throws SecurityException,
	    NoSuchFieldException, IllegalArgumentException,
	    IllegalAccessException {
	Field conn = appender.getClass().getDeclaredField(name);
	conn.setAccessible(true);
	conn.set(appender, value);
    }
}
