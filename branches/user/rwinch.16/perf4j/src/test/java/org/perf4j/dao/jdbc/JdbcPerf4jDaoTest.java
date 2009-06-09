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
package org.perf4j.dao.jdbc;

import java.sql.Connection;
import java.util.Collections;

import javax.sql.PooledConnection;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;

/**
 * Unit tests for {@link JdbcPerf4jDao}. Integration tests should inherit from
 * {@link AbstractJdbcPerf4jDaoITest}.
 * 
 * @author rw012795
 * 
 */
public class JdbcPerf4jDaoTest extends TestCase {

    private JdbcPerf4jDao jdbcPerf4jDao;

    private Mockery mock;

    private Connection connection;

    public void setUp() throws Exception {
	super.setUp();
	this.mock = new Mockery();
	this.connection = this.mock.mock(Connection.class);
	this.jdbcPerf4jDao = new JdbcPerf4jDao(connection);
    }

    public void tearDown() throws Exception {
	mock.assertIsSatisfied();
	super.tearDown();
    }

    // start constructor
    public void testConstructorNullConnection() {
	try {
	    new JdbcPerf4jDao((Connection) null);
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals("connection cannot be null", success.getMessage());
	}
    }

    public void testConstructorNullPooledConnection() {
	try {
	    new JdbcPerf4jDao((PooledConnection) null);
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals("pooledConnection cannot be null", success
		    .getMessage());
	}
    }

    // end constructor

    // start accessors

    public void testGetConnectionFromPool() throws Exception {
	final PooledConnection pooledConnection = mock
		.mock(PooledConnection.class);
	mock.checking(new Expectations() {
	    {
		oneOf(pooledConnection).getConnection();
		will(returnValue(connection));
	    }
	});

	this.jdbcPerf4jDao = new JdbcPerf4jDao(pooledConnection);
	Connection actual = this.jdbcPerf4jDao.getConnection();
	assertEquals(connection, actual);
    }

    public void testGetConnection() throws Exception {
	assertNotNull(this.jdbcPerf4jDao.getConnection());
    }

    // end accessors

    // start saveStopWatches

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
