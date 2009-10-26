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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.TimingStatistics;

/**
 * Abstract class for performing integration tests using the
 * {@link GenericPerf4jJdbcAppender} in different databases.
 * 
 * @author rw012795
 * 
 */
public abstract class GenericPerf4jJdbcAppenderITest extends TestCase {

	protected GenericPerf4jJdbcAppender jdbcPerf4jDao;

	protected StopWatch stopWatch;

	protected GroupedTimingStatistics groupedTimingStatistics;
	
	protected Connection connection;
	
	private DataSource dataSource;

	public void setUp() throws Exception {
		super.setUp();

		this.stopWatch = new StopWatch(1243634525837L, 10L,
				"JdbcPerf4jDaoTest", "JUNIT");
		if(dataSource == null) {
			dataSource = this.createDataSource();
		}
		this.jdbcPerf4jDao = new GenericPerf4jJdbcAppender(dataSource);
		this.jdbcPerf4jDao.setInsertGroupedTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_GROUPEDTIMINGSTATISTICS_SQL);
		this.jdbcPerf4jDao.setInsertStopWatchSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_STOPWATCH_SQL);
		this.jdbcPerf4jDao.setInsertTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_TIMINGSTATISTICS_SQL);
		this.connection = dataSource.getConnection();
		this.tearDownSchema();
		this.setUpSchema();
	}

	public void tearDown() throws Exception {
		this.tearDownSchema();
		connection.commit();
		connection.close();
		super.tearDown();
	}

	protected Connection getConnection() throws SQLException {		
		return connection;
	}

	// start saveStopWatches

	public void testSaveStopWatch() throws Exception {
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	public void testSaveStopWatchDefaultConstructor() throws Exception {
		this.stopWatch = new StopWatch();
		stopWatch.stop(); // have to stop it so that it isn't running
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	public void testSaveStopWatchEmptyMessage() throws Exception {
		this.stopWatch.setMessage("");
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	public void testSaveStopWatchEmptyTag() throws Exception {
		this.stopWatch.setTag("");
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	public void testSaveStopWatchNullMessage() throws Exception {
		this.stopWatch.setMessage(null);
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	public void testSaveStopWatchNullTag() throws Exception {
		this.stopWatch.setTag(null);
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	/**
	 * Make sure we are escaping
	 */
	public void testSaveStopWatchSqlInjection() throws Exception {
		this.stopWatch = new StopWatch(1243634525837L, 10L,
				"testSaveStopWatch\"; drop table perf4j_stopwatches; commit;",
				"JUNIT");
		this.jdbcPerf4jDao.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
		assertStopWatch();
	}

	// end saveStopWatches

	// start saveGroupedTimingStatistics

	public void testSaveGroupedTimingStatistics() throws Exception {
		groupedTimingStatistics = new GroupedTimingStatistics();
		groupedTimingStatistics.setStartTime(stopWatch.getStartTime() - 1000);
		groupedTimingStatistics.setStopTime(groupedTimingStatistics
				.getStartTime()
				+ (1000 * 60 * 5));

		groupedTimingStatistics.addStopWatch(stopWatch);
		StopWatch stopWatch2 = new StopWatch(stopWatch.getStartTime() + 1000,
				123456L, "testSaveGroupedTimingStatistics",
				"this is a second one to test");

		groupedTimingStatistics.addStopWatch(stopWatch2);
		this.jdbcPerf4jDao.saveGroupedTimingStatistics(Collections
				.singleton((GroupedTimingStatistics) groupedTimingStatistics));

		ResultSet results = execute(
				"select * from perf4j_groupedtimingstatistics").getResultSet();
		assertEquals(true, results.next());
		String groupId = assertEquals(groupedTimingStatistics, results);
		assertEquals(false, results.next());

		PreparedStatement ps = getConnection()
				.prepareStatement(
						"select * from perf4j_timingstatistics where perf4j_gts_id = ? order by tag");
		ps.setString(1, groupId);
		results = ps.executeQuery();
		assertEquals(true, results.next());
		assertEquals(groupedTimingStatistics.getStatisticsByTag().get(
				stopWatch.getTag()), results);
		assertEquals(groupId, results.getString(2));
		assertEquals(stopWatch.getTag(), results.getString("tag"));
		assertEquals(true, results.next());
		assertEquals(groupedTimingStatistics.getStatisticsByTag().get(
				stopWatch2.getTag()), results);
		assertEquals(groupId, results.getString(2));
		assertEquals(stopWatch2.getTag(), results.getString("tag"));
		assertEquals(false, results.next());
	}

	// end saveGroupedTimingStatistics

	// utility

	private void assertStopWatch() throws SQLException {
		ResultSet result = execute(
				"select id, elapsed_time, message, start_time, tag from perf4j_stopwatches")
				.getResultSet();
		assertEquals(true, result.next());
		assertEquals(result, this.stopWatch);
		assertEquals(false, result.next());
	}

	protected void assertEquals(ResultSet resultSet, StopWatch stopWatch)
			throws SQLException {
		// resultSet.get(1) is the id
		assertEquals(stopWatch.getElapsedTime(), resultSet.getLong(2));
		assertEquals(convertEmptyString(stopWatch.getMessage()), resultSet
				.getString(3));
		assertEquals(stopWatch.getStartTime(), resultSet.getLong(4));
		assertEquals(convertEmptyString(stopWatch.getTag()), resultSet
				.getString(5));
	}

	/**
	 * Allows subclasses to convert empty String (i.e. Oracle that treats empty
	 * string like null)
	 * 
	 * @param value
	 * @return
	 */
	protected String convertEmptyString(String value) {
		return value;
	}

	protected String assertEquals(GroupedTimingStatistics expected,
			ResultSet actual) throws SQLException {
		// resultSet.get(1) is the id
		assertEquals(expected.getStartTime(), actual.getLong(2));
		assertEquals(expected.getStopTime(), actual.getLong(3));
		return actual.getString(1);
	}

	protected void assertEquals(TimingStatistics timingStatistics,
			ResultSet actual) throws SQLException {
		assertEquals(timingStatistics.getCount(), actual.getLong(3));
		assertEquals(timingStatistics.getMax(), actual.getLong(4));
		assertEquals(timingStatistics.getMean(), actual.getDouble(5));
		assertEquals(timingStatistics.getMin(), actual.getLong(6));
		assertEquals(timingStatistics.getStandardDeviation(), actual
				.getDouble(7));
	}

	protected Statement execute(String sql) throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.execute(sql);
		return stmt;
	}

	protected abstract DataSource createDataSource() throws SQLException;

	protected void setUpSchema() throws SQLException {
		execute("create table perf4j_stopwatches (id varchar(64) primary key, message varchar(32), elapsed_time BIGINT, start_time BIGINT, tag LONGVARCHAR);");
		execute("create table perf4j_groupedtimingstatistics (id varchar(64) primary key, start_time BIGINT, stop_time BIGINT);");
		execute("create table perf4j_timingstatistics (id varchar(64) primary key, perf4j_gts_id varchar(64), count_stat BIGINT, max_stat BIGINT, mean_stat BIGINT, min_stat BIGINT, std_deviation_stat BIGINT, tag LONGVARCHAR);");
		execute("alter table perf4j_timingstatistics add constraint fkgroupedtimingstatistics foreign key (perf4j_gts_id) references perf4j_groupedtimingstatistics (id)");
		execute("create sequence perf4j_sequence start with 1 increment by 1");
	}

	protected void tearDownSchema() throws SQLException {
		executeAll(
				"drop table perf4j_stopwatches cascade constraints",
				"drop table perf4j_groupedtimingstatistics cascade constraints",
				"drop table perf4j_timingstatistics cascade constraints",
				"drop sequence perf4j_sequence");
	}

	protected void executeAll(String... jdbc) {
		for (String stmt : jdbc) {
			try {
				execute(stmt);
			} catch (SQLException e) {
				// e.printStackTrace();
			}
		}
	}
}
