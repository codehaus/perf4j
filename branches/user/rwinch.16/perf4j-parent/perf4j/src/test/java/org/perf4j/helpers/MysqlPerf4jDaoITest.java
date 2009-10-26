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

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * In order for this test to run a MySQL instance must be available at
 * jdbc:mysql://localhost:3306/perf4j. For details on the setup view the
 * documentation in hin-parent.
 * 
 * @author rw012795
 * 
 */
public class MysqlPerf4jDaoITest extends GenericPerf4jJdbcAppenderITest {

	public void tearDown() throws Exception {
		this.tearDownSchema();
	}

	protected void setUpSchema() throws SQLException {
		execute("create table perf4j_stopwatches (id varchar(64) primary key, message VARCHAR(64), elapsed_time BIGINT UNSIGNED, start_time BIGINT UNSIGNED, tag VARCHAR(64))");
		execute("create table perf4j_groupedtimingstatistics (id varchar(64), start_time BIGINT UNSIGNED, stop_time BIGINT UNSIGNED)");
		execute("create table perf4j_timingstatistics (id varchar(64), perf4j_gts_id varchar(64),  count_stat BIGINT UNSIGNED, max_stat BIGINT UNSIGNED, min_stat BIGINT UNSIGNED, mean_stat BIGINT UNSIGNED, std_deviation_stat BIGINT UNSIGNED, tag VARCHAR(64))");
		execute("alter table perf4j_timingstatistics add constraint fkgts foreign key (perf4j_gts_id) references perf4j_groupedtimingstatistics (id)");
		getConnection().commit();
	}

	@Override
	protected void tearDownSchema() throws SQLException {
		executeAll("drop table perf4j_stopwatches cascade",
				"drop table perf4j_groupedtimingstatistics cascade",
				"drop table perf4j_timingstatistics cascade");
	}

	@Override
	protected DataSource createDataSource() throws SQLException {		
		SingleConnectionDataSource ds = new SingleConnectionDataSource();
		ds.setSuppressClose(true);
		ds.setAutoCommit(false);
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setPassword("perf4j");
		ds.setUsername("perf4j");
		ds.setUrl("jdbc:mysql://localhost:3306/perf4j");
		return ds;
	}
}
