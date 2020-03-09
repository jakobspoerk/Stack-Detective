/**
 * Copyright (C) 2011 Johan Andren <johan@markatta.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.markatta.stackdetective.render;

import java.util.ArrayList;
import java.util.List;

import com.markatta.stackdetective.filter.EntryFilter;
import com.markatta.stackdetective.model.Entry;
import com.markatta.stackdetective.model.Segment;
import com.markatta.stackdetective.model.StackTrace;

/**
 * Contains hooks for rendering the various parts of the stack trace. 
 *
 * @author johan
 */
public abstract class AbstractStackTraceRenderer implements StackTraceRenderer {

    private EntryFilter filter;

    public final void setFilter(EntryFilter filter) {
        this.filter = filter;
    }

    @Override
    public final String render(StackTrace stackTrace) {
        final StringBuilder builder = new StringBuilder();
        renderPreTrace(builder, stackTrace);
        final List<Entry> ignoredEntries = new ArrayList<Entry>();

        for (int segmentIndex = 0; segmentIndex < stackTrace.getSegments().size(); segmentIndex++) {

            renderSegment(stackTrace, segmentIndex, builder, ignoredEntries);
        }

        renderPostTrace(builder, stackTrace);
        return builder.toString();
    }

    private void renderIgnoredEntries(List<Entry> ignoredEntries, StringBuilder builder, int entryIndex) {
        // may be invoked with an empty list of ignored entries,
        // make sure we only call renderIgnoredEntries on the subclass if there were
        // any ignored entries
        if (!ignoredEntries.isEmpty()) {
            renderIgnoredEntries(builder, ignoredEntries, entryIndex - ignoredEntries.size());
            ignoredEntries.clear();
        }
    }

    private void renderSegment(final StackTrace stackTrace, int segmentIndex, StringBuilder builder, List<Entry> ignoredEntries) {
        final Segment segment = stackTrace.getSegments().get(segmentIndex);
        
        renderPreSegment(builder, stackTrace.getSegments(), segmentIndex);
        renderEntries(segment, builder, ignoredEntries);
        renderIgnoredEntries(ignoredEntries, builder, segment.numberOfEntries());

        renderPostSegment(builder, stackTrace.getSegments(), segmentIndex);
       
    }

    private void renderEntries(Segment segment, StringBuilder builder, List<Entry> ignoredEntries) {
        for (int entryIndex = 0; entryIndex < segment.numberOfEntries(); entryIndex++) {
            Entry entry = segment.getEntries().get(entryIndex);

            if (filter == null) {
                renderEntry(builder, segment.getEntries(), entryIndex);
            } else if (filter.include(entry, entryIndex)) {
                renderIgnoredEntries(ignoredEntries, builder, entryIndex);
                renderEntry(builder, segment.getEntries(), entryIndex);
            } else {
                ignoredEntries.add(entry);
            }
        }
    }

    /**
     * Add text before iterating over the segments of the stack trace
     * @param builder Add text to this builder
     * @param trace The stack trace
     */
    protected void renderPreTrace(StringBuilder builder, StackTrace trace) {
    }

    /**
     * Add text after iterating over all the segments of the stack trace
     * @param builder Add text to this builder
     * @param trace The stack trace
     */
    protected void renderPostTrace(StringBuilder builder, StackTrace trace) {
    }

    /**
     * Add text before iterating over the entries of the segment. (For example
     * the text of the exception)
     * 
     * @param builder Add text to this builder
     * @param segments The stack trace segments
     * @param segmentIndex The number of the segment in the trace, starting with 0
     */
    protected void renderPreSegment(StringBuilder builder, List<Segment> segments, int segmentIndex) {
    }

    /**
     * Add text after iterating over the entries of the stack trace
     * @param builder Add text to this builder
     * @param segments The stack trace segments
     * @param segmentIndex The number of the segment in the trace, starting with 0
     */
    protected void renderPostSegment(StringBuilder builder, List<Segment> segments, int segmentIndex) {
    }

    /**
     * Add text for one entry. Will not be called if there is a filter that says
     * that the entry should be ignored.
     * 
     * @param builder Add text to this builder
     * @param entries All entries in the segment
     * @param entryIndex  The number of the entry in the segment, starting with 0
     */
    protected void renderEntry(StringBuilder builder, List<Entry> entries, int entryIndex) {
    }

    /**
     * Add text for one or more entries that has been ignored.
     * @param builder Add text to this builder
     * @param ignoredEntries These are the entries that was ignored
     * @param firstEntryIndex the index in the segment of the first entry that was ignored starting with 1
     */
    protected void renderIgnoredEntries(StringBuilder builder, List<Entry> ignoredEntries, int firstEntryIndex) {
    }
}
