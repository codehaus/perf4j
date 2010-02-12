package org.perf4j.aop.jexl;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;
import org.perf4j.aop.AbstractJoinPoint;
import org.perf4j.aop.Profiled;
import org.perf4j.aop.StopWatchMessageGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stop watch's tag and message generator using JEXL parser.
 *
 * @author Alex Devine
 * @author Marcin Zajączkowski, 2010-01-29
 *
 * @since 0.9.13
 */
public class JexlStopWatchMessageGenerator implements StopWatchMessageGenerator {

    /**
     * This Map is used to cache compiled JEXL expressions. While theoretically unbounded, in reality the number of
     * possible keys is equivalent to the number of unique JEXL expressions created in @Profiled annotations, which
     * will have to be loaded in memory anyway when the class is loaded.
     */
    private Map<String, Expression> jexlExpressionCache = new ConcurrentHashMap<String, Expression>(64, .75F, 16);


    /**
     * {@inheritDoc}
     */
    public String getStopWatchTag(Profiled profiled,
                                     AbstractJoinPoint ajp,
                                     Object returnValue,
                                     Throwable exceptionThrown) {
        String tag;
        if (Profiled.DEFAULT_TAG_NAME.equals(profiled.tag())) {
            // if the tag name is not explicitly set on the Profiled annotation,
            // use the name of the method being annotated.
            tag = ajp.getMethodName();
        } else if (profiled.el() && profiled.tag().indexOf("{") >= 0) {
            tag = evaluateJexl(profiled.tag(), ajp.getParameters(), ajp.getTarget(), returnValue, exceptionThrown);
        } else {
            tag = profiled.tag();
        }
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    public String getStopWatchMessage(Profiled profiled,
                                      AbstractJoinPoint pjp,
                                      Object returnValue,
                                      Throwable exceptionThrown) {
        String message;
        if (profiled.el() && profiled.message().indexOf("{") >= 0) {
            message = evaluateJexl(profiled.message(), pjp.getParameters(), pjp.getTarget(), returnValue, exceptionThrown);
            if ("".equals(message)) {
                message = null;
            }
        } else {
            message = "".equals(profiled.message()) ? null : profiled.message();
        }
        return message;
    }


    /**
     * Helper method is used to parse out {expressionLanguage} elements from the text and evaluate the strings using
     * JEXL.
     *
     * @param text            The text to be parsed.
     * @param args            The args that were passed to the method to be profiled.
     * @param annotatedObject The value of the object whose method was profiled. Will be null if a class method was
     *                        profiled.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The evaluated string.
     * @see org.perf4j.aop.Profiled#el()
     */
    protected String evaluateJexl(String text,
                                  Object[] args,
                                  Object annotatedObject,
                                  Object returnValue,
                                  Throwable exceptionThrown) {
        StringBuilder retVal = new StringBuilder(text.length());

        //create a JexlContext to be used in all evaluations
        JexlContext jexlContext = new HashMapContext();
        for (int i = 0; i < args.length; i++) {
            jexlContext.getVars().put("$" + i, args[i]);
        }
        jexlContext.getVars().put("$this", annotatedObject);
        jexlContext.getVars().put("$return", returnValue);
        jexlContext.getVars().put("$exception", exceptionThrown);

        // look for {expression} in the passed in text
        int bracketIndex;
        int lastCloseBracketIndex = -1;
        while ((bracketIndex = text.indexOf('{', lastCloseBracketIndex + 1)) >= 0) {
            retVal.append(text.substring(lastCloseBracketIndex + 1, bracketIndex));

            lastCloseBracketIndex = text.indexOf('}', bracketIndex + 1);
            if (lastCloseBracketIndex == -1) {
                //if there wasn't a closing bracket index just go to the end of the string
                lastCloseBracketIndex = text.length();
            }

            String expressionText = text.substring(bracketIndex + 1, lastCloseBracketIndex);
            if (expressionText.length() > 0) {
                try {
                    Object result = getJexlExpression(expressionText).evaluate(jexlContext);
                    retVal.append(result);
                } catch (Exception e) {
                    //we don't want to propagate exceptions up
                    retVal.append("_EL_ERROR_");
                }
            }
        }

        //append the final part
        if (lastCloseBracketIndex < text.length()) {
            retVal.append(text.substring(lastCloseBracketIndex + 1, text.length()));
        }

        return retVal.toString();
    }

    /**
     * Helper method gets a compiled JEXL expression for the specified expression text, either from the cache or by
     * creating a new compiled expression.
     *
     * @param expressionText The JEXL expression text
     * @return A compiled JEXL expression representing the expression text
     * @throws Exception Thrown if there was an error compiling the expression text
     */
    public Expression getJexlExpression(String expressionText) throws Exception {
        Expression retVal = jexlExpressionCache.get(expressionText);
        if (retVal == null) {
            //Don't need synchronization here - if we end up calling createExpression in 2 separate threads, that's fine
            jexlExpressionCache.put(expressionText, retVal = ExpressionFactory.createExpression(expressionText));
        }
        return retVal;
    }
}
