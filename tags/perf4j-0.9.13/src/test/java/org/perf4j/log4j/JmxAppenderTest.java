/* Copyright (c) 2008-2009 HomeAway, Inc.
 * All rights reserved.  http://www.perf4j.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.perf4j.StopWatch;
import org.perf4j.helpers.StatisticsExposingMBean;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Tests the JmxAttributeStatisticsAppender
 */
public class JmxAppenderTest extends TestCase {
    public void testJmxAppender() throws Exception {
        DOMConfigurator.configure(getClass().getResource("log4jWJmx.xml"));

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName statisticsMBeanName = new ObjectName(StatisticsExposingMBean.DEFAULT_MBEAN_NAME);

        //add a notification listener so we can check for notifications
        DummyNotificationListener notificationListener = new DummyNotificationListener();
        server.addNotificationListener(statisticsMBeanName, notificationListener, null, null);

        //log a bunch of messages
        Logger logger = Logger.getLogger(StopWatch.DEFAULT_LOGGER_NAME);
        for (int i = 0; i < 20; i++) {
            long time = (i % 2) == 0 ? 100L : 200L;
            logger.info(new StopWatch(System.currentTimeMillis(), time, "tag" + (i % 2), "logging"));
            Thread.sleep(110);
        }

        //check that the mbean attributes are accessible
        assertTrue(((Integer) server.getAttribute(statisticsMBeanName, "tag0Count")) > 0);
        assertEquals(0.0, server.getAttribute(statisticsMBeanName, "tag0StdDev"));
        assertEquals(100.0, server.getAttribute(statisticsMBeanName, "tag0Mean"));
        assertEquals(100L, server.getAttribute(statisticsMBeanName, "tag0Min"));
        assertEquals(100L, server.getAttribute(statisticsMBeanName, "tag0Max"));
        assertTrue(((Double) server.getAttribute(statisticsMBeanName, "tag0TPS")) > 1);

        assertTrue(((Integer) server.getAttribute(statisticsMBeanName, "tag1Count")) > 0);
        assertEquals(0.0, server.getAttribute(statisticsMBeanName, "tag1StdDev"));
        assertEquals(200.0, server.getAttribute(statisticsMBeanName, "tag1Mean"));
        assertEquals(200L, server.getAttribute(statisticsMBeanName, "tag1Min"));
        assertEquals(200L, server.getAttribute(statisticsMBeanName, "tag1Max"));
        assertTrue(((Double) server.getAttribute(statisticsMBeanName, "tag1TPS")) > 1);

        //invoke exposeTag, insure that it makes a tag accessible
        server.invoke(statisticsMBeanName, "exposeTag", new Object[]{"tagFoo"}, new String[0]);
        assertEquals(0, server.getAttribute(statisticsMBeanName, "tagFooCount"));

        //invoke removeTag, which should return true
        assertTrue((Boolean) server.invoke(statisticsMBeanName,
                                           "removeTag",
                                           new Object[]{"tagFoo"},
                                           new String[] {String.class.getName()}));

        //now at a stopwatch that should trigger a notification
        logger.info(new StopWatch(System.currentTimeMillis(), 20000L, "tag0", "logging"));
        Thread.sleep(1100L); //go over the next time slice boundary
        logger.info(new StopWatch(System.currentTimeMillis(), 20000L, "tag0", "logging"));

        //check for the notification - need the wait loop as it takes time for the notification to appear
        for (int i = 0; i <= 200; i++) {
            Thread.sleep(10);
            if (notificationListener.lastReceivedNotification != null) {
                assertEquals(StatisticsExposingMBean.OUT_OF_RANGE_NOTIFICATION_TYPE,
                             notificationListener.lastReceivedNotification.getType());
                break;
            }

            if (i == 200) {
                fail("Notification not received after 2 seconds");
            }
        }
    }

    protected static class DummyNotificationListener implements NotificationListener {
        public Notification lastReceivedNotification;

        public void handleNotification(Notification notification, Object handback) {
            lastReceivedNotification = notification;
        }
    }
}
