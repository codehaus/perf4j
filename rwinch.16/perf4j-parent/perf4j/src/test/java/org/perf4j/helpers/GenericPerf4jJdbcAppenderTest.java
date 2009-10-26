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
package org.perf4j.helpers;

import java.util.Collections;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.jmock.Mockery;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;

/**
 * Unit tests for {@link JdbcPerf4jDao}. Integration tests should inherit from
 * {@link GenericPerf4jJdbcAppenderITest}.
 * 
 * @author rw012795
 * 
 */
public class GenericPerf4jJdbcAppenderTest extends TestCase {

	private GenericPerf4jJdbcAppender jdbcPerf4jDao;

	private Mockery mock;

	private DataSource connection;

	public void setUp() throws Exception {
		super.setUp();
		this.mock = new Mockery();
		this.connection = this.mock.mock(DataSource.class);
		this.jdbcPerf4jDao = new GenericPerf4jJdbcAppender(connection);
		initDefaultSql();
	}
	
	private void initDefaultSql() {
		this.jdbcPerf4jDao.setInsertGroupedTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_GROUPEDTIMINGSTATISTICS_SQL);
		this.jdbcPerf4jDao.setInsertStopWatchSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_STOPWATCH_SQL);
		this.jdbcPerf4jDao.setInsertTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_TIMINGSTATISTICS_SQL);
	}

	public void tearDown() throws Exception {
		mock.assertIsSatisfied();
		super.tearDown();
	}

	// start constructor
	public void testConstructorNullConnection() {
		try {
			new GenericPerf4jJdbcAppender((DataSource) null);
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("dataSource cannot be null", success.getMessage());
		}
	}

	// end constructor

	// start saveStopWatches
	
	public void testSaveStopWatchesNullDataSource() {
		this.jdbcPerf4jDao = new GenericPerf4jJdbcAppender();
		this.initDefaultSql();
		try {
			this.jdbcPerf4jDao.saveStopWatches(Collections.singleton(new StopWatch()));			
			fail("expected exception");
		}catch(IllegalStateException success) {
			assertEquals("cannot aquire a connection with a null dataSource. Call setDataSource first.",success.getMessage());
		}
	}

	public void testSaveStopWatchesNull() throws Exception {
		try {
			this.jdbcPerf4jDao.saveStopWatches(null);
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("stopWatches cannot be null", success.getMessage());
		}
	}

	public void testSaveStopWatchesContainsNull() throws Exception {
		try {
			this.jdbcPerf4jDao.saveStopWatches(Collections
					.singleton((StopWatch) null));
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("stopWatches cannot contain null value. Got [null]",
					success.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public void testSaveStopWatchesEmpty() throws Exception {
		try {
			this.jdbcPerf4jDao.saveStopWatches(Collections.EMPTY_SET);
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("stopWatches cannot be empty", success.getMessage());
		}
	}

	// end saveStopWatches

	// start saveGroupedTimingStatistics
	
	public void testSaveGroupedTimingstatisticsNullDataSource() {
		this.jdbcPerf4jDao = new GenericPerf4jJdbcAppender();
		initDefaultSql();
		try {
			this.jdbcPerf4jDao.saveGroupedTimingStatistics(Collections.singleton(new GroupedTimingStatistics()));
			fail("expected exception");
		}catch(IllegalStateException success) {
			assertEquals("cannot aquire a connection with a null dataSource. Call setDataSource first.",success.getMessage());
		}
	}

	public void testSaveGroupedTimingStatisticsNull() throws Exception {
		try {
			this.jdbcPerf4jDao.saveGroupedTimingStatistics(null);
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("groupedTimingStatistics cannot be null", success
					.getMessage());
		}
	}

	public void testSaveGroupedTimingStatisticsContainsNull() throws Exception {
		try {
			this.jdbcPerf4jDao.saveGroupedTimingStatistics(Collections
					.singleton((GroupedTimingStatistics) null));
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals(
					"groupedTimingStatistics cannot contain null value. Got [null]",
					success.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public void testSaveGroupedTimingStatisticsEmpty() throws Exception {
		try {
			this.jdbcPerf4jDao
					.saveGroupedTimingStatistics(Collections.EMPTY_SET);
			fail("Expected Exception");
		} catch (IllegalArgumentException success) {
			assertEquals("groupedTimingStatistics cannot be empty", success
					.getMessage());
		}
	}

}
