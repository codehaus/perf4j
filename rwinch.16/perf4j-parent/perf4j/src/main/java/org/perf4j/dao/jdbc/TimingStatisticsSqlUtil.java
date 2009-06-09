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

import org.perf4j.TimingStatistics;

/**
 * This class is meant for internal use only and provides utility methods for
 * creating TimingStatistics from a ResultSet.
 * 
 * @author rw012795
 * 
 */
class TimingStatisticsSqlUtil {

    /**
     * Gets the tag for the TimingStatistics from a ResultSet.
     * 
     * @param resultSet
     *            the ResultSet for a TimingStatistics. This must contain a
     *            column named "tag".
     * @return the tag for a TimingStatistics
     * @throws SQLException
     *             if it could not get the tag from the TimingStatistics
     * @throws IllegalArgumentException
     *             if resultSet is null
     */
    public static String getTag(ResultSet resultSet) throws SQLException {
	if (resultSet == null) {
	    throw new IllegalArgumentException("resultSet cannot be null");
	}
	return resultSet.getString(tsColumn("tag"));
    }

    /**
     * Creates a TimingStatistics from a ResultSet.
     * 
     * @param resultSet
     *            the ResultSet to create a TimingStatistics from. This should
     *            be a ResultSet from the per4j_timingstatistics table. Cannot
     *            be null.
     * @return a TimingStatistics from the given ResultSet
     * @throws SQLException
     *             if a database error occured when creating the
     *             TimingStatistics
     * @throws IllegalArgumentException
     *             if resultSet is null.
     */
    public static TimingStatistics createTimingStatistics(ResultSet resultSet)
	    throws SQLException {
	if (resultSet == null) {
	    throw new IllegalArgumentException("resultSet cannot be null");
	}
	double mean = resultSet.getDouble(tsColumn("mean_stat"));
	double stdDeviation = resultSet
		.getDouble(tsColumn("std_deviation_stat"));
	long max = resultSet.getLong(tsColumn("max_stat"));
	long min = resultSet.getLong(tsColumn("min_stat"));
	int count = resultSet.getInt(tsColumn("count_stat"));
	return new TimingStatistics(mean, stdDeviation, max, min, count);
    }

    /**
     * Creates a columnName for the {@link TimingStatistics} results.
     * 
     * @param columnName
     *            the name of the column
     * @return the name of the column for a {@link TimingStatistics} result.
     */
    private static String tsColumn(String columnName) {
	return columnName; // TODO this should include the table name, but
	// breaks some vendors
    }

    /**
     * Ensure it can't be instantiated
     */
    private TimingStatisticsSqlUtil() {
    }
}
