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

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;

import com.mysql.jdbc.Connection;

public class AbstractPerf4jJdbcLoggerTest extends TestCase {

	private AbstractPerf4jJdbcAppender appender;

	private Mockery mock;
	
	private DataSource dataSource;
	
	public void setUp() {
		mock = new Mockery();	
		dataSource = mock.mock(DataSource.class);
		appender = new AbstractPerf4jJdbcAppender() {
			@Override
			protected DataSource createDataSource() throws Exception {
				return dataSource;
			}			
		};
		appender.activateOptions();
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

	public void testAppendMessageGroupedTimingStatistics() throws Exception {
		final GroupedTimingStatistics gts = new GroupedTimingStatistics();
		mock.checking(new Expectations() {
			{
				final Connection connection = mock.mock(Connection.class);
				one(dataSource).getConnection();
				will(returnValue(connection));
				allowing(connection).prepareStatement(with(any(String.class)));
				one(connection).commit();
				one(connection).close();
			}
		});
		appender.append(createEvent(gts));
	}

	public void testAppendMessageStopWatch() throws Exception {
		final StopWatch stopWatch = new StopWatch("tag", "message");
		stopWatch.stop();

		mock.checking(new Expectations() {
			{
				final Connection connection = mock.mock(Connection.class);
				one(dataSource).getConnection();
				will(returnValue(connection));
				allowing(connection).prepareStatement(with(any(String.class)));
				one(connection).commit();
				one(connection).close();
			}
		});
		appender.append(createEvent(stopWatch));
	}

	// end append

	// start setPerf4jDao

	public void testActivateOptionsNullDataSource() {
		appender = new AbstractPerf4jJdbcAppender() {				
			@Override
			protected DataSource createDataSource() throws Exception {
				return null;
			}
		};		
		try {
			appender.activateOptions();
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("dataSource cannot be null", success.getMessage());
		}
	}

	// end setPerf4jDao

	

	// start activateOptions

//	public void testActivateParametersNoParametersSet() {
//		appender = new AbstractPerf4jJdbcAppender();
//		try {
//			appender.activateOptions();
//			fail("Expected Exception");
//		} catch (RuntimeException success) {
//			assertEquals(
//					"Couldn't create a connection with the parameters databaseUrl=null, databaseUser=null, databasePassword=null",
//					success.getMessage());
//		}
//	}
//
//	public void testActivateParametersPerf4jDaoSetAlready() {
//		final Perf4jDao expected = appender.getPerf4jDao();
//		appender.activateOptions();
//		assertEquals(expected, appender.getPerf4jDao());
//	}
//
//	public void testActivateParameters() {
//		appender = new AbstractPerf4jJdbcAppender();
//		assertNull(appender.getPerf4jDao());
//
//		appender.setDatabasePassword("");
//		appender.setDatabaseUrl("jdbc:h2:target/h2/perf4j");
//		appender.setDriver("org.h2.Driver");
//		appender.setDatabaseUser("sa");
//
//		appender.activateOptions();
//		assertNotNull(appender.getPerf4jDao());
//	}

	// end activateOptions

	// -- helper methods --

	private LoggingEvent createEvent(Object message) {
		return new LoggingEvent(Logger.class.getName(), Logger
				.getLogger(StopWatch.DEFAULT_LOGGER_NAME), System
				.currentTimeMillis(), Level.INFO, message, null);
	}

//	private void setField(String name, Object value) throws SecurityException,
//			NoSuchFieldException, IllegalArgumentException,
//			IllegalAccessException {
//		Field conn = appender.getClass().getDeclaredField(name);
//		conn.setAccessible(true);
//		conn.set(appender, value);
//	}
}
