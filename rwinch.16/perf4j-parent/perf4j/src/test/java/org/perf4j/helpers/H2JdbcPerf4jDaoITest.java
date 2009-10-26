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

import org.h2.jdbcx.JdbcDataSource;

/**
 * Tests an H2 Database
 * 
 * @author rw012795
 * 
 */
public class H2JdbcPerf4jDaoITest extends GenericPerf4jJdbcAppenderITest {

	@Override
	protected DataSource createDataSource() throws SQLException {
		JdbcDataSource result = new JdbcDataSource();
		result.setPassword("");
		result.setUser("sa");
		result.setURL("jdbc:h2:target/h2/perf4j");
		return result;
	}
}
