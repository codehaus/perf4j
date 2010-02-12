package org.perf4j.log4j.aop;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.perf4j.LoggingStopWatch;
import org.perf4j.aop.AgnosticAbstractTimingAspect;
import org.perf4j.log4j.Log4JStopWatch;

/**
 * PoC implementation of AgnosticAbstractTimingAspect for Log4j.
 *
 * @author Marcin Zajączkowski, 2010-01-29
 */
public class AgnosticTimingAspect extends AgnosticAbstractTimingAspect {

    @Override
    protected LoggingStopWatch newStopWatch(String loggerName, String levelName) {
        Level level = Level.toLevel(levelName, Level.INFO);
        return new Log4JStopWatch(Logger.getLogger(loggerName), level, level);
    }
}
