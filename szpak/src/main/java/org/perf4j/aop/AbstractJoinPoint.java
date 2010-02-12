package org.perf4j.aop;

/**
 * AOP agnostic join point.
 *
 * An AOP implementation agnostic interface which offers all information required to do a measure, proceed original
 * method and log result in customizable way. Specific Join Point implementations in AOP libraries/frameworks
 * should implement it wrapping their own internal structures.
 *
 * TODO: MZA: Think about using some existing standards (like AOP Alliance)
 *
 * TODO: MZA: something more is needed to build a tag/message for StopWatch?
 * 
 * @author Marcin Zajączkowski, 2010-01-14
 *
 * @since 0.9.13
 */
public interface AbstractJoinPoint {

    /**
     * Calls profiled method and returns its result.
     *
     * @return result of proceeding
     * @throws Exception thrown exception
     */
    public Object proceed() throws Exception;

    /**
     * An object whose method was annotated (profiled).
     *
     * @return an object whose method was annotated
     */
    public Object getTarget();

    /**
     * Returns an parameters (arguments) array of prcoessing method
     *
     * @return array of parameters
     */
    public Object[] getParameters();

    /**
     * Returns a processing method name.
     *
     * @return processing method name
     */
    public String getMethodName();
}
