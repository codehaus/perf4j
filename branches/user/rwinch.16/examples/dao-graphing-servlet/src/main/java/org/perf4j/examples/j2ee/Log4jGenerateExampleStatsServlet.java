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
package org.perf4j.examples.j2ee;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

/**
 * Sleeps for a specified amount of time for a specified tag and in a
 * Log4jStopWatch. The http parameter tag will change the default tag of
 * firstBlock and sleep (in ms) will override the default sleep time of 1000ms.
 * 
 * @author rw012795
 * 
 */
public class Log4jGenerateExampleStatsServlet extends HttpServlet {

    protected void service(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {

	String tag = request.getParameter("tag");
	if (tag == null) {
	    tag = "firstBlock";
	}

	long sleep = 1000;

	try {
	    sleep = Long.parseLong(request.getParameter("sleep"));
	} catch (NumberFormatException e) {
	}

	StopWatch stopWatch = new Log4JStopWatch(tag);
	try {
	    Thread.sleep(sleep);
	} catch (InterruptedException e) {

	} finally {
	    stopWatch.stop();
	}

	request.setAttribute("tag",tag);
	request.setAttribute("sleep", sleep);
	request.getRequestDispatcher("generate.jsp").forward(request,response);
    }

    private static final long serialVersionUID = -5141412018616760265L;
}
