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

import junit.framework.TestCase;

/**
 * Unit tests for {@link GroupedTimingStatisticsSqlUtil}. Integration tests are
 * performed running the *JdbcPerf4jDaoITest.
 * 
 * @author rw012795
 * 
 */
public class GroupedTimingStatisticsSqlUtilTest extends TestCase {

    public void testCreateGroupedTimingStatisticsNullResultSet()
	    throws Exception {
	try {
	    GroupedTimingStatisticsSqlUtil.createGroupedTimingStatistics(null);
	    fail("Expected Exception");
	} catch (IllegalArgumentException success) {
	    assertEquals("resultSet cannot be null", success.getMessage());
	}
    }
}
