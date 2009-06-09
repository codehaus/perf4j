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
package org.perf4j.dao;

import java.util.Set;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.TimingStatistics;

/**
 * Provides a way of persisting {@link GroupedTimingStatistics} and
 * {@link TimingStatistics}. Iplementations could range from flat files, to
 * jdbc, to JPA, etc.
 * 
 * @author rw012795
 * 
 */
public interface Perf4jDao {

    /**
     * Saves a Set of StopWatches to the database.
     * 
     * @param stopWatches
     *            the stopWatches to save. Cannot be null, empty, or contain
     *            null values.
     * @return the set of ids that were generated for the stopWatches passed in.
     * @throws IllegalArgumentException
     *             if stopWatches is invalid.
     * @throws RuntimeException
     *             if an error occured when saving to the database.
     */
    Set<String> saveStopWatches(Set<StopWatch> stopWatches);

    /**
     * Saves the groupedTimingStatistics to a database.
     * 
     * @param groupedTimingStatistics
     *            the {@link GroupedTimingStatistics} to save to the database
     *            Cannot be null, contain null values, or be empty.
     * @throws IllegalArgumentException
     *             if groupedTimingStatistics is null, contains null values, or
     *             is empty
     * @throw RuntimeException if an error occurred saving to the database
     */
    Set<String> saveGroupedTimingStatistics(
	    Set<GroupedTimingStatistics> groupedTimingStatistics);

}