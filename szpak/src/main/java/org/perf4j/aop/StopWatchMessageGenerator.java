package org.perf4j.aop;

/**
 * Provides methods for stop watch's tag and message generation. 
 *
 * @author Alex Devine
 * @author Marcin Zajączkowski, 2010-01-29
 *
 * @since 0.9.13
 *
 * FIXME: MZA: give it a better name
 */
public interface StopWatchMessageGenerator {

    /**
     * Helper method gets the tag to use for StopWatch logging. Performs JEXL evaluation if necessary.
     *
     * @param profiled        The profiled annotation that was attached to the method.
     * @param pjp             The ProceedingJoinPoint encapulates the method around which this aspect advice runs.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The value to use as the StopWatch tag.
     */
    public String getStopWatchTag(Profiled profiled,
                                  AbstractJoinPoint pjp,
                                  Object returnValue,
                                  Throwable exceptionThrown);

    /**
     * Helper method get the message to use for StopWatch logging. Performs JEXL evaluation if necessary.
     *
     * @param profiled        The profiled annotation that was attached to the method.
     * @param pjp             The ProceedingJoinPoint encapulates the method around which this aspect advice runs.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The value to use as the StopWatch message.
     */
    public String getStopWatchMessage(Profiled profiled,
                                      AbstractJoinPoint pjp,
                                      Object returnValue,
                                      Throwable exceptionThrown);
}
