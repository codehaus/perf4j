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

import java.sql.SQLException;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

/**
 * For this test to work you need to setup an oracle database at localhost an
 * sid of xe, with username perf4j, & password perf4j. I know this test may be a
 * pain to get setup initially, but please leave it as I use Oracle and want to
 * ensure it works. See perf4j-parent for details on setup requirements.
 * 
 * @author rw012795
 * 
 */
public class Oracle10gPerf4jDaoITest extends GenericPerf4jJdbcAppenderITest {

	@Override
	protected DataSource createDataSource() throws SQLException {
		OracleDataSource ds = new OracleDataSource();
		ds.setPassword("perf4j");
		ds.setUser("perf4j");
		ds.setURL("jdbc:oracle:thin:@localhost:1521:xe");
		ds.setConnectionCachingEnabled(true);
		return ds;
	}

	@Override
	protected void setUpSchema() throws SQLException {
		execute("create table perf4j_stopwatches (id varchar2(64) primary key, elapsed_time number(38), message VARCHAR2(128), start_time number(38), tag VARCHAR2(128))");
		execute("create table perf4j_groupedtimingstatistics (id varchar2(64) primary key, start_time number(38), stop_time number(38), tag VARCHAR2(128))");
		execute("create table perf4j_timingstatistics (id varchar2(64) primary key, perf4j_gts_id varchar2(64),count_stat number(38), max_stat number(38), mean_stat number(38), min_stat number(38), std_deviation_stat number(38), tag VARCHAR2(128))");
		execute("alter table perf4j_timingstatistics add constraint fkgts foreign key (perf4j_gts_id) references perf4j_groupedtimingstatistics (id)");
		execute("create sequence perf4j_sequence start with 1 increment by 1 nomaxvalue");
	}

	/**
	 * Oracle treats empty strings as an empty string. This is actually a bit
	 * annoying, but hopefully acceptable.
	 */
	@Override
	protected String convertEmptyString(String value) {
		return value == null || "".equals(value) ? null : value;
	}

}
