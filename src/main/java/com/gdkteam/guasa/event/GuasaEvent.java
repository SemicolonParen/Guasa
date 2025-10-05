/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.event;

import com.gdkteam.guasa.memory.HeapAnalyzer;

public abstract class GuasaEvent {
    private final long timestamp;

    protected GuasaEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static class CoreStarted extends GuasaEvent {}

    public static class CoreStopped extends GuasaEvent {}

    public static class ObjectTracked extends GuasaEvent {
        private final long objectId;
        private final String className;

        public ObjectTracked(long objectId, String className) {
            this.objectId = objectId;
            this.className = className;
        }

        public long getObjectId() { return objectId; }
        public String getClassName() { return className; }
    }

    public static class ObjectUntracked extends GuasaEvent {
        private final long objectId;

        public ObjectUntracked(long objectId) {
            this.objectId = objectId;
        }

        public long getObjectId() { return objectId; }
    }

    public static class HeapAnalyzed extends GuasaEvent {
        private final HeapAnalyzer.HeapSnapshot snapshot;

        public HeapAnalyzed(HeapAnalyzer.HeapSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public HeapAnalyzer.HeapSnapshot getSnapshot() { return snapshot; }
    }

    public static class ReferenceGraphUpdated extends GuasaEvent {}

    public static class AnalysisComplete extends GuasaEvent {}

    public static class ObjectSelected extends GuasaEvent {
        private final long objectId;

        public ObjectSelected(long objectId) {
            this.objectId = objectId;
        }

        public long getObjectId() { return objectId; }
    }

    public static class FilterChanged extends GuasaEvent {
        private final String filterText;

        public FilterChanged(String filterText) {
            this.filterText = filterText;
        }

        public String getFilterText() { return filterText; }
    }
}
