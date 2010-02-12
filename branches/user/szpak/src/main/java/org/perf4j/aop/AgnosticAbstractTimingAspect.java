package org.perf4j.aop;

import org.perf4j.LoggingStopWatch;
import org.perf4j.aop.jexl.JexlStopWatchMessageGenerator;

/**
 * AOP implementation agnostic base class for TimingAspects.
 *
 * Subclasses just need to implement the {@link #newStopWatch} method to use their logging framework
 * of choice (e.g. log4j or java.logging) to persist the StopWatch log message.
 * </p>
 * Subclasses for specific logging frameworks can be used directly by a client framework in their interceptors,
 * wrapping framework AOP specific JoinPoint implementation into AbstractJoinPoint.
 * </p>
 * For natively supported AOP providers (like AOP) delivered subclass can be used instead of.
 *
 * @author Alex Devine
 * @author Marcin Zajączkowski, 2010-01-29
 *
 * @since 0.9.13
 */
public abstract class AgnosticAbstractTimingAspect {

    private StopWatchMessageGenerator messageGenerator;

    /**
     * Default constructor using JEXL for message generation
     */
    protected AgnosticAbstractTimingAspect() {
        this(new JexlStopWatchMessageGenerator());
    }

    /**
     * Constructor with ability to use custom parser for message generation.
     *
     * @param messageGenerator stop watch message generator to use
     */
    protected AgnosticAbstractTimingAspect(StopWatchMessageGenerator messageGenerator) {
        this.messageGenerator = messageGenerator;
    }

    /**
     * This advice is used to add the StopWatch logging statements around method executions that have been tagged
     * with the Profiled annotation.
     *
     * @param ajp The ProceedingJoinPoint encapulates the method around which this aspect advice runs.
     * @param profiled The profiled annotation that was attached to the method.
     *
     * @return The return value from the method that was executed.
     *
     * @throws Exception Any exceptions thrown by the underlying method.
     */
    public Object doPerfLogging(AbstractJoinPoint ajp, Profiled profiled) throws Exception {

        //WORKAROUND - the + "" below is needed to workaround a bug in the AspectJ ajc compiler that generates invalid
        //bytecode causing AbstractMethodErrors.
        LoggingStopWatch stopWatch = newStopWatch(profiled.logger() + "", profiled.level());

        //if we're not going to end up logging the stopwatch, just run the wrapped method
        if (!stopWatch.isLogging()) {
            return ajp.proceed();
        }

        stopWatch.setTimeThreshold(profiled.timeThreshold());

        Object retVal = null;
        Exception exceptionThrown = null;
        try {
            return retVal = ajp.proceed();
        } catch (Exception t) {
            throw exceptionThrown = t;
        } finally {
            String tag = messageGenerator.getStopWatchTag(profiled, ajp, retVal, exceptionThrown);
            String message = messageGenerator.getStopWatchMessage(profiled, ajp, retVal, exceptionThrown);

            if (profiled.logFailuresSeparately()) {
                tag = (exceptionThrown == null) ? tag + ".success" : tag + ".failure";
            }

            stopWatch.stop(tag, message);
        }
    }

    /**
     * Subclasses should implement this method to return a LoggingStopWatch that should be used to time the wrapped
     * code block.
     *
     * @param loggerName The name of the logger to use for persisting StopWatch messages.
     * @param levelName  The level at which the message should be logged.
     * @return The new LoggingStopWatch.
     */
    protected abstract LoggingStopWatch newStopWatch(String loggerName, String levelName);

    //TODO: maybe add generic getStopWatchTag() and getStopWatchMessage() to be able to override them?
}
