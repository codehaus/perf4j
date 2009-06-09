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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.dao.Perf4jDao;
import org.perf4j.dao.jdbc.JdbcPerf4jDao;

/**
 * This should be attached to a {@link AsyncCoalescingStatisticsAppender} as a
 * downstream appender. It will append any LogginEvent message that is of type
 * {@link GroupedTimingStatistics} or {@link StopWatch} to the database. An
 * example configuration for this appender specified in the package level
 * documentation.
 * 
 * TODO add jndi location as way of configuring the connection
 * 
 * @author rw012795
 * 
 */
public final class Perf4jDaoAppender extends AppenderSkeleton {

    /**
     * The url of the database to connect to
     */
    private String databaseUrl;

    /**
     * The username to user to connect to the database
     */
    private String databaseUser;

    /**
     * The password of the user to connect to the database
     */
    private String databasePassword;

    /**
     * The connection to use for the database
     */
    private Connection connection;

    /**
     * The Perf4jDao to save StopWatch and GroupedTimingStatistics with.
     */
    private Perf4jDao perf4jDao;

    // --- overrides ---

    /**
     * Gets the message from loggingEvent. If the message is of type
     * GroupedTimingStatistics or StopWatch it is saved using the Perf4jDao.
     * 
     * TODO optimize with batch submits? (probably create an impl of Perf4jDao that has a buffer)
     * @param  logginEvent the event to log.
     * @throws IllegalArgumentException if loggingEvent is null
     * 
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
	if(loggingEvent == null) {
	    throw new IllegalArgumentException("loggingEvent cannot be null");
	}
	Object message = loggingEvent.getMessage();

	// TODO have boolean flag to disable saving gts, so only StopWatches can
	// be saved
	if ((message instanceof GroupedTimingStatistics)) {
	    GroupedTimingStatistics groupedTimingStatistics = (GroupedTimingStatistics) message;
	    this.perf4jDao.saveGroupedTimingStatistics(Collections
		    .singleton(groupedTimingStatistics));

	}

	// TODO have boolean flag to disable stopwatch from being saved so only
	// gts can be saved
	if ((message instanceof StopWatch)) {
	    StopWatch stopWatch = (StopWatch) message;
	    this.perf4jDao.saveStopWatches(Collections
		    .singleton((StopWatch) stopWatch));
	}
    }

    /**
     * If {@link Perf4jDao} is null, creates a new Connection and initializes
     * the Perf4jDao with it.
     */
    @Override
    public synchronized void activateOptions() {
	super.activateOptions();

	this.closed = false;
	if (this.perf4jDao == null) {
	    this.connection = createConnection();
	    // TODO create via reflection so that the implementation can be
	    // swapped out
	    this.perf4jDao = new JdbcPerf4jDao(connection);
	}
    }

    /**
     * Closes the database connection
     */
    @Override
    public void close() {
	if (this.closed) {
	    return;
	}
	try {
	    if(connection != null) {
		connection.close();
	    }
	} catch (Exception e) {
	    getErrorHandler().error(
		    "Couldn't close connection on shutdown. Reason: "
			    + e.getMessage(), e, ErrorCode.CLOSE_FAILURE);
	}
	this.closed = true;
    }

    @Override
    public boolean requiresLayout() {
	return false;
    }

    // --- bean properties ---

    /**
     * Ensures that the given driver class has been loaded for sql connection
     * creation.
     */
    public void setDriver(String driverClass) {
	try {
	    Class.forName(driverClass);
	} catch (Exception e) {
	    errorHandler.error("Failed to load driver", e,
		    ErrorCode.GENERIC_FAILURE);
	    throw new IllegalArgumentException("Could not load driver "+driverClass,e);
	}
    }

    /**
     * Sets the url database.
     * 
     * @param databaseUrl
     *            The url to connect to the database. i.e.)
     *            jdbc:mysql://localhost:3306/perf4j
     */
    public void setDatabaseUrl(String databaseUrl) {
	this.databaseUrl = databaseUrl;
    }

    /**
     * Sets the username to access the database
     * 
     * @param databaseUser
     *            The username to connect to the database
     */
    public void setDatabaseUser(String databaseUser) {
	this.databaseUser = databaseUser;
    }

    /**
     * Sets the password used to access the database
     * 
     * @param databasePassword
     *            The password to connect to the database
     */
    public void setDatabasePassword(String databasePassword) {
	this.databasePassword = databasePassword;
    }

    /**
     * Sets the {@link Perf4jDao} to use.
     * @param perf4jDao the new {@link Perf4jDao} to use.
     * @throws IllegalArgumentException if perf4jDao is null.
     */
    public void setPerf4jDao(Perf4jDao perf4jDao) {
	if(perf4jDao == null) {
	    throw new IllegalArgumentException("perf4jDao cannot be null");
	}
        this.perf4jDao = perf4jDao;
    }
    
    /**
     * Gets the {@link Perf4jDao} that is being used.
     * @return
     */
    public Perf4jDao getPerf4jDao() {
	return this.perf4jDao;
    }
    

    // -- private helper methods --

    /**
     * If a connection has been set, returns it. Otherwise, checks to see if a
     * database driver has been loaded, if not it attempts to load
     * <code>sun.jdbc.odbc.JdbcOdbcDriver</code>. Then a database connection is
     * created using <code>databaseUrl</code>, <code>databaseUser</code>,
     * <code>databasePassword</code>.
     * 
     * @throws RuntimeException
     *             if a Connection could not be created.
     */
    private Connection createConnection() {
	if (!DriverManager.getDrivers().hasMoreElements()) {
	    // try and load a default driver
	    setDriver("sun.jdbc.odbc.JdbcOdbcDriver");
	}

	// TODO allow a properties file to be specified so that other
	// configuration options can be specified. For example, MySql complains
	// about autocommit being true when a manual commit is being done
	try {
	    return DriverManager.getConnection(databaseUrl, databaseUser,
		    databasePassword);
	} catch (Exception e) {
	    throw new RuntimeException(
		    "Couldn't create a connection with the parameters databaseUrl="
			    + databaseUrl + ", databaseUser=" + databaseUser
			    + ", databasePassword=" + databasePassword, e);
	}
    }
}
