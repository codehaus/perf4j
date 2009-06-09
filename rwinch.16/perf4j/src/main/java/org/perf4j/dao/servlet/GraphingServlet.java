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
package org.perf4j.dao.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.chart.GoogleChartGenerator;
import org.perf4j.chart.StatisticsChartGenerator;
import org.perf4j.dao.Perf4jDao;
import org.perf4j.dao.jdbc.JdbcPerf4jDao;
import org.perf4j.helpers.StatsValueRetriever;
import org.perf4j.servlet.AbstractGraphingServlet;

/**
 * <p>
 * <b>WARNING:</b> This is still being prototyped out. It needs to be optimized
 * so that we can query for a specific date range as it will not scale. <b>USE
 * WITH CAUTION & KNOWING THIS WILL CHANGE IN NON-PASSIVE WAYS</b>
 * </p>
 * 
 * <p>
 * This graphing servlet implementation looks for graphs using a
 * {@link Perf4jDao} implementation. Graph names are parsed out using a
 * predefined format. For details on the format see
 * {@link #getGraphByName(String)}.
 * </p>
 * 
 * <p>
 * Initialization parameters for this are:
 * 
 * <ul>
 * <li>DatabaseUrl - the url to the database (i.e. jdbc:h2:target/h2/perf4j)</li>
 * <li>DatabaseUser - the username to connect to the database</li>
 * <li>DatabasePassword - the password to connect to the database</li>
 * <li>Driver - the database driver to load (i.e. org.h2.Driver)</li>
 * <li>Schema - the optional resource to use to load the schema into the
 * database. This should not be used in production environments.</li>
 * </ul>
 * 
 * An example configuration is given below:
 * 
 * <pre>
 * 	&lt;servlet&gt;
 *         &lt;servlet-name&gt;perf4j&lt;/servlet-name&gt;
 *         &lt;servlet-class&gt;org.perf4j.dao.servlet.GraphingServlet&lt;/servlet-class&gt;        
 *         &lt;!--
 *           The graphNames parameter determines which graphs to expose. See the
 *           javadoc of this class for details on the format.
 *         --&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;graphNames&lt;/param-name&gt;
 *             &lt;param-value&gt;Mean:firstBlock:secondBlock,TPS:firstBlock:secondBlock&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;DatabaseUrl&lt;/param-name&gt;
 *             &lt;param-value&gt;jdbc:h2:target/h2/perf4j&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;DatabaseUsername&lt;/param-name&gt;
 *             &lt;param-value&gt;sa&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;DatabasePassword&lt;/param-name&gt;
 *             &lt;param-value&gt;password&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;Driver&lt;/param-name&gt;
 *             &lt;param-value&gt;org.h2.Driver&lt;/param-value&gt;
 *         &lt;/init-param&gt;	
 *         &lt;!--
 *           h2.ddl is a resource on the classpath that contains the ddl to import
 *           (each statement must be a single line) 
 *         --&gt;	
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;Schema&lt;/param-name&gt;
 *             &lt;param-value&gt;h2.ddl&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *     &lt;/servlet&gt;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * FIXME This implementation is not ready for production as the queries will not
 * scale
 * </p>
 * <p>
 * TODO add unit tests
 * </p>
 * <p>
 * TODO allow jndi configuration of datasource
 * </p>
 * @author rw012795
 * 
 */
public class GraphingServlet extends AbstractGraphingServlet {

    /**
     * This is used to retrieve the {@link GroupedTimingStatistics} from a
     * database. Please not that it is using a very ineffective means of
     * retrieval right now and will not scale in a production environment.
     * 
     * FIXME this should eventually be the interface.
     */
    private JdbcPerf4jDao perf4jDao;

    /**
     * The connection used by <code>perf4jDao</code> to retrieve
     * {@link GroupedTimingStatistics}
     */
    private Connection connection;

    /**
     * Initializes connection and the perf4jDao.
     */
    @Override
    public void init() throws ServletException {
	super.init();
	try {
	    initDriver();
	    connection = createJdbcConnection();
	    importSchema();
	} catch (Exception e) {
	    throw new ServletException(e.getMessage(), e);
	}
	this.perf4jDao = new JdbcPerf4jDao(connection);
    }

    /**
     * Performs a close on the connection.
     */
    @Override
    public void destroy() {
	super.destroy();
	try {
	    this.connection.close();
	} catch (SQLException sqle) {
	    throw new RuntimeException("Failed to close down the connection",
		    sqle);
	}
    }
    
    // -- AbstractGraphingServlet overrides --

    /**
     * Do not tell all the graph names as they are infinate.
     */
    @Override
    protected List<String> getAllKnownGraphNames() {
	return Collections.emptyList();
    }

    /**
     * Creates a GoogleChartGenerator using the Perf4jDao and a specialized
     * format for a graph name.
     * 
     * TODO probably should look into overriding other methods (just implemented the required one)
     * 
     * @param name
     *            the name of the graph according to the specified format. The
     *            format of <code>name</code> is &lt;stats_value_retriever_type
     *            &gt;:&ltenabled_tag1&gt;:&lt;enabled_tag2&gt;... An example
     *            graph name might be TPS:firstBlock:secondBlock. This would
     *            create a {@link GoogleChartGenerator} using the
     *            <code>StatsValueRetriever.TPS_VALUE_RETRIEVER</code> with the
     *            tags firstBlock and secondBlock enabled.
     */
    @Override
    protected StatisticsChartGenerator getGraphByName(String name) {

	String type = name.substring(0, name.indexOf(':'));
	String[] tags = name.substring(name.indexOf(':') + 1, name.length())
		.split(":");

	StatsValueRetriever statsValueRetriever = StatsValueRetriever.DEFAULT_RETRIEVERS
		.get(type);
	GoogleChartGenerator statsChartGenerator = new GoogleChartGenerator(
		statsValueRetriever);
	Set<String> enabledTags = new HashSet<String>(Arrays.asList(tags));
	statsChartGenerator.setEnabledTags(enabledTags);
	Collection<GroupedTimingStatistics> groupedTimingStatistics;
	try {
	    groupedTimingStatistics = this.perf4jDao
		    .getGroupedTimingStatistics();
	} catch (SQLException e) {
	    throw new RuntimeException(e.getMessage(), e);
	}
	for (GroupedTimingStatistics statistics : groupedTimingStatistics) {
	    statsChartGenerator.appendData(statistics);
	}
	return statsChartGenerator;
    }

    // -- helper methods --

    /**
     * Initializes the driver.
     */
    private void initDriver() {
	String driverName = getInitParameter("Driver");
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	try {
	    Class.forName(driverName, true, loader);
	} catch (Exception e) {
	    throw new RuntimeException("Couldn't load database driver "
		    + driverName, e);
	}
    }
    
    // -- sql utility methods --

    /**
     * Creates a jdbc connection from the init parameters passed in.
     * 
     * @return
     * @throws SQLException
     */
    private Connection createJdbcConnection() throws SQLException {
	String url = getInitParameter("DatabaseUrl");
	String username = getInitParameter("DatabaseUsername");
	String password = getInitParameter("DatabasePassword");

	try {
	    return DriverManager.getConnection(url, username, password);
	} catch (Exception e) {
	    throw new SQLException("Couldn't create connection using " + url
		    + ", " + username + ", " + password);
	}
    }

    /**
     * If specified the init parameter Schema is specified, uses schema as a
     * resource name and imports the schema using that resource.
     * 
     * @throws SQLException
     * @throws IOException
     */
    private void importSchema() throws SQLException, IOException {
	String schema = getInitParameter("Schema");
	if (schema == null) {
	    return;
	}
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	InputStream in = loader.getResourceAsStream(schema);
	if (in == null) {
	    throw new IOException(
		    "Couldn't import schema from classpath resource " + schema
			    + ". The resource is not found");
	}
	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	try {
	    for (String line = reader.readLine(); line != null; line = reader
		    .readLine()) {
		execute(line);
	    }
	    connection.commit();
	} finally {
	    reader.close();
	}
    }

    /**
     * Executes a line of sql
     * 
     * @param sql
     * @throws SQLException
     */
    private void execute(String sql) throws SQLException {
	Statement stmt = connection.createStatement();
	try {
	    stmt.execute(sql);
	} finally {
	    stmt.close();
	}
    }
    
    private static final long serialVersionUID = -2343949335910030445L;
}
