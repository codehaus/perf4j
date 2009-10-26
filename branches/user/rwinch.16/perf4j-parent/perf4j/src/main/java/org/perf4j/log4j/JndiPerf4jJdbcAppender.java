package org.perf4j.log4j;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Creates an {@link AbstractPerf4jJdbcAppender} that creates its DataSource
 * from a JNDI location. 
 * 
 * @author rw012795
 * 
 */
public final class JndiPerf4jJdbcAppender extends AbstractPerf4jJdbcAppender {

	private String jndiLocation;

	// --- accessor methods ---

	/**
	 * The JNDI location of the DataSource
	 * 
	 * @param jndiLocation
	 *            the JNDI location of the DataSource to use. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if jndiLocation is null.
	 */
	public void setJndiLocation(String jndiLocation) {
		if(jndiLocation == null) {
			throw new IllegalArgumentException("jndiLocation cannot be null");
		}
		this.jndiLocation = jndiLocation;
	}

	// --- AbstractPerf4jJdbcAppender methods ---

	@Override
	protected DataSource createDataSource() throws NamingException {
		if(this.jndiLocation == null) {
			throw new IllegalStateException("jndiLocation cannot be null. Please set it first");
		}
		InitialContext context = new InitialContext();
		try {
			return (DataSource) context.lookup(this.jndiLocation);
		} catch (NamingException namingException) {
			throw (NamingException) new NamingException(
					"Could not lookup a DataSource at " + this.jndiLocation)
					.initCause(namingException);
		}
	}
}
