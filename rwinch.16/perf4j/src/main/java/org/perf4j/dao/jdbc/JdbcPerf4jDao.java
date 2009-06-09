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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.PooledConnection;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.TimingStatistics;
import org.perf4j.dao.Perf4jDao;

/**
 * 
 * Perf4jDao that looks allows saving and retreival of
 * {@link GroupedTimingStatistics} and {@link StopWatch} using jdbc.
 * 
 * TODO change all get by index to get by column name
 * 
 * @author rw012795
 */
public class JdbcPerf4jDao implements Perf4jDao {

    /**
     * The connection to use for jdbc transations. Either this or the
     * PooledConnection will not be null.
     */
    private Connection connection;

    /**
     * A pooled connection to use for jdbc transactions. Either this or the
     * connection will not be null.
     */
    private PooledConnection pooledConnection;

    /**
     * If true, the connection will be closed after each method. Otherwise
     * connections will remain open.
     */
    private boolean closeConnection;

    /**
     * Creates a new JdbcPerf4jDao with a PooledConnection
     * 
     * @param pooledConnection
     *            the PooledConnection to use. Cannot be null.
     * @throws IllegalArgumentException
     *             if pooledConnection is null
     */
    public JdbcPerf4jDao(PooledConnection pooledConnection) {
	if (pooledConnection == null) {
	    throw new IllegalArgumentException(
		    "pooledConnection cannot be null");
	}
	this.pooledConnection = pooledConnection;
	this.closeConnection = true;
    }

    /**
     * Creates a new JdbcPerf4jDao with a Connection
     * 
     * @param connection
     *            the database connection to be used. Cannot be null.
     * @throws IllegalArgumentException
     *             if connection is null
     */
    public JdbcPerf4jDao(Connection connection) {
	if (connection == null) {
	    throw new IllegalArgumentException("connection cannot be null");
	}
	this.connection = connection;
    }

    /**
     * TODO This method is only on the implementation because I need to
     * investigate how these need to be queried. Possible parameters are below.
     * 
     * @param start
     *            the GroupedTimingStatstics.start must be after this
     * @param end
     *            the GroupedTimingStatistics.end must be before this
     * @param tags
     *            will only get the timing statistics with these tags. If
     *            includeRollup will get rollup for these stats too.
     * @param includeRollup
     *            if true, get the rollup stats for the tags provided will be
     *            included as well.
     * @return TODO remove throws SQLException
     */
    public Collection<GroupedTimingStatistics> getGroupedTimingStatistics()
	    throws SQLException {
	// TODO allow this to be injected
	// TODO the order by was a quick attempt to fix the way the charts were being generated...failed :(
	String groupedTimingStats = "select gts.id, gts.start_time, gts.stop_time, ts.count_stat, ts.max_stat, ts.mean_stat, ts.min_stat, ts.std_deviation_stat, ts.tag from perf4j_groupedtimingstatistics gts, perf4j_timingstatistics ts where ts.perf4j_gts_id = gts.id order by gts.start_time";
	Connection connection = getConnection();
	try {
	    PreparedStatement ps = connection
		    .prepareStatement(groupedTimingStats);
	    try {
		ResultSet resultSet = ps.executeQuery();
		return GroupedTimingStatisticsSqlUtil
			.createGroupedTimingStatistics(resultSet);
	    } finally {
		ps.close();
	    }
	} finally {
	    if (closeConnection) {
		connection.close();
	    }
	}
    }

    /**
     * @see org.perf4j.dao.jdbc.Perf4jDao#saveStopWatches(java.util.Set)
     */
    public Set<String> saveStopWatches(Set<StopWatch> stopWatches) {
	try {
	    return performSaveStopWatches(stopWatches);
	} catch (SQLException failure) {
	    throw new RuntimeException(failure);
	}

    }

    /**
     * Saves the stopWatches to the database. See {@link #saveStopWatches(Set)}.
     * 
     * @param stopWatches
     * @return
     * @throws SQLException
     *             if a problem occurred when saving to the database.
     */
    private Set<String> performSaveStopWatches(Set<StopWatch> stopWatches)
	    throws SQLException {
	validateCollection("stopWatches", stopWatches);

	// TODO allow this to be injected
	String insertStopWatchSql = "insert into perf4j_stopwatches (id, elapsed_time, message, start_time, tag) values (?, ?, ?, ?, ?)";
	Set<String> stopWatchIds = new HashSet<String>(stopWatches.size());
	Connection connection = getConnection();
	try {
	    for (StopWatch stopWatch : stopWatches) {
		String stopWatchId = generateId(connection);
		PreparedStatement ps = connection
			.prepareStatement(insertStopWatchSql);
		try {
		    ps.setString(1, stopWatchId);
		    ps.setLong(2, stopWatch.getElapsedTime());
		    ps.setString(3, stopWatch.getMessage());
		    ps.setLong(4, stopWatch.getStartTime());
		    ps.setString(5, stopWatch.getTag());
		    ps.executeUpdate();
		    stopWatchIds.add(stopWatchId);
		} finally {
		    ps.close();
		}
	    }
	    connection.commit();
	} finally {
	    if (closeConnection) {
		connection.close();
	    }
	}
	return stopWatchIds;
    }

    /**
     * @see org.perf4j.dao.jdbc.Perf4jDao#saveGroupedTimingStatistics(java.util.Set)
     */
    public Set<String> saveGroupedTimingStatistics(
	    Set<GroupedTimingStatistics> groupedTimingStatistics) {
	try {
	    return performSaveGroupedTimingStatistics(groupedTimingStatistics);
	} catch (SQLException sql) {
	    throw new RuntimeException(sql);
	}
    }

    /**
     * Saves the groupedTimingStatistics to the database. See
     * {@link #saveGroupedTimingStatistics(Set)}.
     * 
     * @param groupedTimingStatistics
     * @return
     * @throws SQLException
     *             if an error occured when saving to the database.
     */
    private Set<String> performSaveGroupedTimingStatistics(
	    Set<GroupedTimingStatistics> groupedTimingStatistics)
	    throws SQLException {
	validateCollection("groupedTimingStatistics", groupedTimingStatistics);

	final String insertGroupedTimingStats = "insert into perf4j_groupedtimingstatistics (id, start_time, stop_time) values (?, ?, ?)";
	final Set<String> gtsIds = new HashSet<String>(groupedTimingStatistics
		.size());
	final Connection connection = getConnection();
	try {
	    for (GroupedTimingStatistics gts : groupedTimingStatistics) {
		String gtsId = generateId(connection);
		PreparedStatement ps = connection
			.prepareStatement(insertGroupedTimingStats);
		try {
		    ps.setString(1, gtsId);
		    ps.setLong(2, gts.getStartTime());
		    ps.setLong(3, gts.getStopTime());
		    ps.executeUpdate();
		    insertTimingStatisticsByName(connection, gtsId, gts);
		    gtsIds.add(gtsId);
		} finally {
		    ps.close();
		}
	    }
	    connection.commit();
	} finally {
	    if (closeConnection) {
		connection.close();
	    }
	}
	return gtsIds;
    }

    /**
     * Saves the timingStatistics in the groupedTimingStatistics.
     * 
     * @param connection
     *            the Connection to use to create the PreparedStatement
     * @param groupedTimingStatisticsId
     *            the id of the {@link GroupedTimingStatistics} that is
     *            associated with the {@link GroupedTimingStatistics} whos
     *            {@link TimingStatistics} is being inserted.
     * @param groupedTimingStatistics
     *            the {@link GroupedTimingStatistics} to save the
     *            {@link TimingStatistics} from.
     */
    private void insertTimingStatisticsByName(Connection connection,
	    String groupedTimingStatisticsId,
	    GroupedTimingStatistics groupedTimingStatistics)
	    throws SQLException {

	// TODO allow this to be injected
	String insertTimingStats = "insert into perf4j_timingstatistics (id, perf4j_gts_id, count_stat, max_stat, mean_stat, min_stat, std_deviation_stat, tag) values (?, ?, ?, ?, ?, ?, ?, ?)";

	for (Map.Entry<String, TimingStatistics> entry : groupedTimingStatistics
		.getStatisticsByTag().entrySet()) {
	    String tag = entry.getKey();
	    TimingStatistics timingStatistics = entry.getValue();
	    PreparedStatement ps = connection
		    .prepareStatement(insertTimingStats);
	    try {
		ps.setString(1, generateId(connection));
		ps.setString(2, groupedTimingStatisticsId);
		ps.setLong(3, timingStatistics.getCount());
		ps.setLong(4, timingStatistics.getMax());
		ps.setDouble(5, timingStatistics.getMean());
		ps.setLong(6, timingStatistics.getMin());
		ps.setDouble(7, timingStatistics.getStandardDeviation());
		ps.setString(8, tag);
		ps.executeUpdate();
	    } finally {
		ps.close();
	    }
	}
    }

    // -- utility methods --

    /**
     * Gets a non-null connection. If Connection is null,
     * pooledConnection.getConnection() is called. Otherwise the connection is
     * returned.
     * 
     */
    protected final Connection getConnection() throws SQLException {
	return this.connection == null ? this.pooledConnection.getConnection()
		: connection;
    }

    /**
     * Generate a new unique id. This method could be overridden by a subclass
     * to use a sequence.
     * 
     * @param connection
     *            the current open connection. Cannot be null.
     * @return a unique id.
     */
    protected String generateId(Connection connection) {
	return UUID.randomUUID().toString();
    }

    /**
     * Validates that a collection is not null, not empty, and does not contain
     * null values.
     * 
     * @param collectionName
     *            the name of the collection that will be used in errror
     *            messages
     * @param collection
     *            the collection to check
     * @throws IllegalArgumentException
     *             if the collection is not valid
     */
    private static void validateCollection(String collectionName,
	    Collection<?> collection) {
	if (collection == null) {
	    throw new IllegalArgumentException(collectionName
		    + " cannot be null");
	}
	if (collection.isEmpty()) {
	    throw new IllegalArgumentException(collectionName
		    + " cannot be empty");
	}
	if (collection.contains(null)) {
	    throw new IllegalArgumentException(collectionName
		    + " cannot contain null value. Got " + collection);
	}
    }

}
