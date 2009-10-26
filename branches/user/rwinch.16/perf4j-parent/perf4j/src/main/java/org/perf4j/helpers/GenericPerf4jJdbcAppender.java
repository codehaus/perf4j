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
package org.perf4j.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.TimingStatistics;

/**
 * 
 * Dao that allows saving of {@link GroupedTimingStatistics} and
 * {@link StopWatch} using jdbc.
 * 
 * TODO talk about the default sql (databases tested against and schemas).
 * 
 * @author rw012795
 */
public class GenericPerf4jJdbcAppender {

	// --- static members ---

	/**
	 * The default sql used to insert a {@link GroupedTimingStatistics}. See
	 * class level documentation for details on what databases are supported and
	 * schema.
	 */
	public static final String DEFAULT_INSERT_GROUPEDTIMINGSTATISTICS_SQL = "insert into perf4j_groupedtimingstatistics (id, start_time, stop_time) values (?, ?, ?)";

	/**
	 * The default sql used to insert a {@link TimingStatistics}. See class
	 * level documentation for details on what databases are supported and
	 * schema.
	 */
	public static final String DEFAULT_INSERT_TIMINGSTATISTICS_SQL = "insert into perf4j_timingstatistics (id, perf4j_gts_id, count_stat, max_stat, mean_stat, min_stat, std_deviation_stat, tag) values (?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * The default sql used to insert a {@link StopWatch}. See class level
	 * documentation for details on what databases are supported and schema.
	 */
	public static final String DEFAULT_INSERT_STOPWATCH_SQL = "insert into perf4j_stopwatches (id, elapsed_time, message, start_time, tag) values (?, ?, ?, ?, ?)";

	// --- members ---

	/**
	 * The DataSource to use for jdbc transactions. Cannot be null.
	 */
	private DataSource dataSource;

	/**
	 * The sql used to insert GroupedTimingStatistics. See setter method for
	 * more details.
	 */
	private String insertGroupedTimingStatsSql;

	/**
	 * The sql used to insert TimingStatistics. See setter method for more
	 * details.
	 */
	private String insertTimingStatsSql;

	/**
	 * The sql used to insert StopWatch Objects. See setter method for more
	 * details.
	 */
	private String insertStopWatchSql;

	/**
	 * Creates a new GenericPerf4jJdbcAppender with a given DataSource.
	 * 
	 * @param dataSource
	 *            the DataSource to use. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if dataSource is null
	 */
	public GenericPerf4jJdbcAppender(DataSource dataSource) {
		setDataSource(dataSource);
	}

	/**
	 * Create a new {@link GenericPerf4jJdbcAppender}.
	 */
	public GenericPerf4jJdbcAppender() {
	}

	/**
	 * If insertStopWatchSql is not null, saves the stopWatches to the database.
	 * Otherwise nothing is saved and an empty Set is returned.
	 * @return the ids of the StopWatch objects that were saved.
	 */
	public Set<String> saveStopWatches(Set<StopWatch> stopWatches) {
		if (insertStopWatchSql == null) {
			return Collections.emptySet();
		}
		try {
			return performSaveStopWatches(stopWatches);
		} catch (SQLException failure) {
			throw new RuntimeException(failure);
		}

	}

	/**
	 * See {@link #saveStopWatches(Set)}.
	 * 
	 * @param stopWatches
	 * @return
	 * @throws SQLException
	 *             if a problem occurred when saving to the database.
	 */
	private Set<String> performSaveStopWatches(Set<StopWatch> stopWatches)
			throws SQLException {
		validateCollection("stopWatches", stopWatches);

		final Set<String> stopWatchIds = new HashSet<String>(stopWatches.size());
		final Connection connection = getConnection();
		// TODO connection.getMetaData().supportsBatchUpdates()
		final PreparedStatement ps = connection
				.prepareStatement(insertStopWatchSql);
		try {
			for (StopWatch stopWatch : stopWatches) {
				final String stopWatchId = generateId(connection);
				ps.setString(1, stopWatchId);
				ps.setLong(2, stopWatch.getElapsedTime());
				ps.setString(3, stopWatch.getMessage());
				ps.setLong(4, stopWatch.getStartTime());
				ps.setString(5, stopWatch.getTag());
				ps.addBatch();
				stopWatchIds.add(stopWatchId);
			}
			ps.executeBatch();
			connection.commit();
		} finally {
			// FIXME add try catch
			ps.close();
			connection.close();
		}
		return stopWatchIds;
	}

	/**
	 * If both insertGroupedTimingStatsSql and are not insertTimingStatsSql
	 * null, saves the stopWatches to the database. Otherwise nothing is saved
	 * and an empty Set is returned.
	 * @return the ids of the GroupedTimingStatistics that are saved
	 */
	public Set<String> saveGroupedTimingStatistics(
			Set<GroupedTimingStatistics> groupedTimingStatistics) {
		if (insertGroupedTimingStatsSql == null || insertTimingStatsSql == null) {
			return Collections.emptySet();
		}
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

		final Set<String> gtsIds = new HashSet<String>(groupedTimingStatistics
				.size());
		final Connection connection = getConnection();
		final PreparedStatement ps = connection
				.prepareStatement(insertGroupedTimingStatsSql);
		final PreparedStatement timingStatsPs = connection
				.prepareStatement(insertTimingStatsSql);
		try {
			for (GroupedTimingStatistics gts : groupedTimingStatistics) {
				String gtsId = generateId(connection);
				ps.setString(1, gtsId);
				ps.setLong(2, gts.getStartTime());
				ps.setLong(3, gts.getStopTime());
				ps.addBatch();
				insertTimingStatisticsByName(connection, timingStatsPs, gtsId,
						gts);
				gtsIds.add(gtsId);
			}
		} finally {
			// FIXME ensure close is always called
			ps.executeBatch();
			timingStatsPs.executeBatch();
			connection.commit();
			timingStatsPs.close();
			ps.close();
			connection.close();
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
			PreparedStatement ps, String groupedTimingStatisticsId,
			GroupedTimingStatistics groupedTimingStatistics)
			throws SQLException {

		for (Map.Entry<String, TimingStatistics> entry : groupedTimingStatistics
				.getStatisticsByTag().entrySet()) {
			String tag = entry.getKey();
			TimingStatistics timingStatistics = entry.getValue();
			ps.setString(1, generateId(connection));
			ps.setString(2, groupedTimingStatisticsId);
			ps.setLong(3, timingStatistics.getCount());
			ps.setLong(4, timingStatistics.getMax());
			ps.setDouble(5, timingStatistics.getMean());
			ps.setLong(6, timingStatistics.getMin());
			ps.setDouble(7, timingStatistics.getStandardDeviation());
			ps.setString(8, tag);
			ps.addBatch();
		}
	}

	// --- accessor methods ---

	/**
	 * Sets the DataSource that should be used.
	 * 
	 * @param dataSource
	 *            the dataSource to set
	 * @throws IllegalArgumentException
	 *             if dataSource is null
	 */
	public void setDataSource(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource cannot be null");
		}
		this.dataSource = dataSource;
	}

	/**
	 * Gets a Connection from the DataSource
	 * 
	 * @return non-null Connection
	 * @throws SQLException
	 * @throws IllegalStateException
	 *             if the DataSource is null.
	 */
	private Connection getConnection() throws SQLException {
		DataSource dataSource = this.dataSource;
		if (dataSource == null) {
			throw new IllegalStateException(
					"cannot aquire a connection with a null dataSource. Call setDataSource first.");
		}
		return dataSource.getConnection();
	}

	/**
	 * The SQL used to insert {@link GroupedTimingStatistics}. If
	 * insertGroupedTimingStatsSql is null, , neither
	 * {@link GroupedTimingStatistics} nor {@link TimingStatistics} objects will
	 * not be inserted. The SQL must use eight place holders ordered in the
	 * following order:
	 * <ul>
	 * <li>GroupedTimingStatistics generated id. (type String)
	 * <li>
	 * <li>GroupedTimingStatistics.getStartTime() (type Long)</li>
	 * <li>GroupedTimingStatistics.getStopTime() (type Long)</li>
	 * </ul>
	 * 
	 * @param insertGroupedTimingStatsSql
	 *            the insertGroupedTimingStatsSql to set
	 */
	public void setInsertGroupedTimingStatsSql(
			String insertGroupedTimingStatsSql) {
		this.insertGroupedTimingStatsSql = insertGroupedTimingStatsSql;
	}

	/**
	 * Sets the sql used to insert a {@link TimingStatistics}. If
	 * insertTimingStatsSql is null, neither {@link GroupedTimingStatistics} nor
	 * {@link TimingStatistics} objects will not be inserted. The sql must use
	 * eight place holders ordered for the {@link TimingStatistics}s attributes
	 * in the following order:
	 * <ul>
	 * <li>TimingStaticis generated id (type String)</li>
	 * <li>GroupedTimingStatistics foreign key (type String)</li>
	 * <li>TimingStatistics.getCount() (type Long)</li>
	 * <li>TimingStatistics.getMax() (type Long)</li>
	 * <li>TimingStatistics.getMean() (type Double)</li>
	 * <li>TimingStatistics.getMin() (type Long)</li>
	 * <li>TimingStaticis.getStandardDeviation() (type Double).</li>
	 * </ul>
	 * 
	 * @param insertTimingStatsSql
	 *            the sql used to insert a {@link TimingStatistics}
	 */
	public void setInsertTimingStatsSql(String insertTimingStatsSql) {
		this.insertTimingStatsSql = insertTimingStatsSql;
	}

	/**
	 * Sets the sql used to insert a {@link StopWatch}. If insertStopWatchSql is null, {@link StopWatch} objects
	 * will not be inserted. The sql must use five
	 * place holders ordered for the {@link StopWatch}s attributes in the
	 * following order: 
	 * <ul>
	 * <li>StopWatch generated id (type String)</li>
	 * <li>StopWatch.getElapsedTime() (type Long)</li>
	 * <li>StopWatch.getMessage() (type String)</li>
	 * <li>StopWatch.getStartTime() (type Long)</li>
	 * <li>StopWatch.getTag() (type String)</li>
	 * </ul> 
	 * 
	 * @param insertStopWatchSql
	 *            the sql used to insert a {@link StopWatch}.
	 */
	public void setInsertStopWatchSql(String insertStopWatchSql) {
		this.insertStopWatchSql = insertStopWatchSql;
	}

	/**
	 * @return the insertGroupedTimingStatsSql
	 */
	public String getInsertGroupedTimingStatsSql() {
		return insertGroupedTimingStatsSql;
	}

	/**
	 * @return the insertTimingStatsSql
	 */
	public String getInsertTimingStatsSql() {
		return insertTimingStatsSql;
	}

	/**
	 * @return the insertStopWatchSql
	 */
	public String getInsertStopWatchSql() {
		return insertStopWatchSql;
	}

	// --- utility methods ---

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
