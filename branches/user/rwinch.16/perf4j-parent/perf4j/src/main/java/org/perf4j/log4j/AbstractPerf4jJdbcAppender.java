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
package org.perf4j.log4j;

import java.util.Collections;

import javax.sql.DataSource;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.TimingStatistics;
import org.perf4j.helpers.GenericPerf4jJdbcAppender;
import org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender.StopWatchEventHandler;

/**
 * Subclasses of this class should be attached to a
 * {@link AsyncCoalescingStatisticsAppender} as a downstream appender. It will
 * append any LogginEvent message that is of type
 * {@link GroupedTimingStatistics} or {@link StopWatch} to the database. An
 * example configuration for this appender specified in the package level
 * documentation.
 * 
 * @author rw012795
 * 
 */
public abstract class AbstractPerf4jJdbcAppender extends AppenderSkeleton implements StopWatchEventHandler {
	
	/**
	 * The DataSource to use for the database
	 */
	private DataSource dataSource;

	/**
	 * Used to save StopWatch and GroupedTimingStatistics with.
	 */
	private GenericPerf4jJdbcAppender jdbcAppender = new GenericPerf4jJdbcAppender();

	// --- overrides ---

	/**
	 * Gets the message from loggingEvent. If the message is of type
	 * GroupedTimingStatistics or StopWatch it is saved using the Perf4jDao.
	 * 
	 * TODO optimize with batch submits? (probably create an impl of Perf4jDao
	 * that has a buffer)
	 * 
	 * @param logginEvent
	 *            the event to log.
	 * @throws IllegalArgumentException
	 *             if loggingEvent is null
	 * 
	 */
	@Override
	protected void append(LoggingEvent loggingEvent) {
		if (loggingEvent == null) {
			throw new IllegalArgumentException("loggingEvent cannot be null");
		}
		Object message = loggingEvent.getMessage();

		if (message instanceof GroupedTimingStatistics) {
			GroupedTimingStatistics groupedTimingStatistics = (GroupedTimingStatistics) message;
			this.jdbcAppender.saveGroupedTimingStatistics(Collections
					.singleton(groupedTimingStatistics));
		}
	}
	
	

	/* (non-Javadoc)
	 * @see org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender.StopWatchEventHandler#error(java.lang.String)
	 */
	public void error(String errorMessage) {
		getErrorHandler().error(errorMessage);
	}

	/**
	 * @see org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender.StopWatchEventHandler#handle(org.perf4j.StopWatch)
	 */
	public void handle(StopWatch stopWatch) {
		if(stopWatch == null) {
			return;
		}
		this.jdbcAppender.saveStopWatches(Collections
				.singleton((StopWatch) stopWatch));
	}



	/**
	 * If {@link Perf4jDao} is null, creates a new Connection and initializes
	 * the Perf4jDao with it.
	 */
	@Override
	public synchronized void activateOptions() {
		super.activateOptions();

		this.closed = false;
		try {
			this.dataSource = createDataSource();
		} catch (Exception e) {
			throw new RuntimeException("Could not create dataSource", e);
		}
		
		this.jdbcAppender.setDataSource(this.dataSource);
	}

	/**
	 * Subclasses should override if they have any cleanup to do.
	 */
	@Override
	public void close() {
		
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	// --- bean properties ---

	/**
	 * If value is true, will set the insertStopWatchSql to be
	 * {@link GenericPerf4jJdbcAppender#DEFAULT_INSERT_STOPWATCH_SQL}. 
	 * 
	 * @param useDefaultStopWatchSql
	 */
	public void setUseDefaultStopWatchSql(boolean useDefaultStopWatchSql) {
		if(useDefaultStopWatchSql) {
			setInsertStopWatchSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_STOPWATCH_SQL);
		}
	}

	/**
	 * If value is true, will set the insertGroupedTimingStatsSql
	 * {@link GenericPerf4jJdbcAppender#DEFAULT_INSERT_GROUPEDTIMINGSTATISTICS_SQL}
	 * and the insertTimingstatsSql to be
	 * {@link GenericPerf4jJdbcAppender#DEFAULT_INSERT_TIMINGSTATISTICS_SQL}
	 * 
	 * @param useDefaultTimingStatsSql
	 */
	public void setUseDefaultTimingStatsSql(boolean useDefaultTimingStatsSql) {
		if(useDefaultTimingStatsSql) {
			setInsertGroupedTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_GROUPEDTIMINGSTATISTICS_SQL);
			setInsertTimingStatsSql(GenericPerf4jJdbcAppender.DEFAULT_INSERT_TIMINGSTATISTICS_SQL);
		}
	}

	/**
	 * Sets the sql used to insert a StopWatch See
	 * {@link GenericPerf4jJdbcAppender#setInsertStopWatchSql(String)} for
	 * details.
	 * 
	 * @param insertStopWatchSql the sql used to insert a {@link StopWatch}.
	 * @see #setUseDefaultStopWatchSql(boolean)
	 */
	public void setInsertStopWatchSql(String insertStopWatchSql) {
		jdbcAppender.setInsertStopWatchSql(insertStopWatchSql);
	}

	/**
	 * Sets the sql used to insert {@link GroupedTimingStatistics}. See
	 * {@link GenericPerf4jJdbcAppender#setInsertGroupedTimingStatsSql(String)}
	 * for details.
	 * 
	 * @param insertGroupedTimingStatsSql the sql used to insert a {@link GroupedTimingStatistics}.
	 * @see #setUseDefaultTimingStatsSql(boolean)
	 */
	public void setInsertGroupedTimingStatsSql(String insertGroupedTimingStatsSql) {
		jdbcAppender.setInsertGroupedTimingStatsSql(insertGroupedTimingStatsSql);
	}
	
	/**
	 * Sets the sql used to insert {@link TimingStatistics}. See
	 * {@link GenericPerf4jJdbcAppender#setInsertTimingStatsSql(String)}
	 * for details.
	 * 
	 * @param insertTimingStatsSql the sql used to insert {@link TimingStatistics}.
	 * @see #setUseDefaultTimingStatsSql(boolean)
	 */
	public void setInsertTimingStatsSql(String insertTimingStatsSql) {
		jdbcAppender.setInsertTimingStatsSql(insertTimingStatsSql);		
	}
	
	// -- abstract methods --

	/**
	 * Subclasses should override this method to create a DataSource.
	 * 
	 * @throws Exception
	 *             if a Connection could not be created.
	 */
	protected abstract DataSource createDataSource() throws Exception;
}
