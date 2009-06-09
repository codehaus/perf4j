/* Licensed under the Apache License, Version 2.0 (the "License");
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.TimingStatistics;

/**
 * This class is for internal use only and provides utility methods for creating
 * {@link GroupedTimingStatistics} from ResultSet.
 * 
 * @author rw012795
 * 
 */
class GroupedTimingStatisticsSqlUtil {

    /**
     * Creates a {@link GroupedTimingStatistics} from a ResultSet that was a
     * query on the perf4j_groupedtimingstatistics
     * 
     * @param resultSet
     *            the ResultSet of a query on the per4j_groupedtimingstatistics
     *            table.
     * @return a {@link GroupedTimingStatistics} from the ResultSet
     * @throws SQLException
     *             if a database error occurred when creating the
     *             {@link GroupedTimingStatistics}.
     * @throws IllegalArgumentException
     *             if resultSet is null.
     */
    public static Collection<GroupedTimingStatistics> createGroupedTimingStatistics(
	    ResultSet resultSet) throws SQLException {
	if (resultSet == null) {
	    throw new IllegalArgumentException("resultSet cannot be null");
	}

	Map<String, GroupedTimingStatistics> idToGroupedTimingStatistics = new HashMap<String, GroupedTimingStatistics>();
	while (resultSet.next()) {
	    String gtsId = resultSet.getString(gtsColumn("id"));
	    boolean createRollupStats = false;
	    long startTime = resultSet.getLong("start_time");
	    long stopTime = resultSet.getLong(gtsColumn("stop_time"));
	    GroupedTimingStatistics gts = idToGroupedTimingStatistics
		    .get(gtsId);
	    if (gts == null) {
		gts = new GroupedTimingStatistics();
		gts.setCreateRollupStatistics(createRollupStats);
		gts.setStartTime(startTime);
		gts.setStopTime(stopTime);
		idToGroupedTimingStatistics.put(gtsId, gts);
	    }
	    String tag = TimingStatisticsSqlUtil.getTag(resultSet);
	    TimingStatistics ts = TimingStatisticsSqlUtil
		    .createTimingStatistics(resultSet);
	    gts.getStatisticsByTag().put(tag, ts);
	}
	return idToGroupedTimingStatistics.values();
    }

    // -- helper methods --

    /**
     * Gets the name of a column for the {@link GroupedTimingStatistics}.
     * 
     * TODO investigate returning the table name or alias to be more specific.
     * This appears to cause problems in some databases though.
     */
    private static String gtsColumn(String name) {
	return "" + name; // perf4j_groupedtimingstatistics.
    }

    /**
     * Ensure it cannot be insantiated
     */
    private GroupedTimingStatisticsSqlUtil() {
    }
}
