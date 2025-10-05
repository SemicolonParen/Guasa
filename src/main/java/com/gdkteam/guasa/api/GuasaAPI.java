/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.api;

import com.gdkteam.guasa.Guasa;
import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.memory.ObjectTracker;
import com.gdkteam.guasa.memory.ReferenceGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GuasaAPI {
    private static final Logger logger = LoggerFactory.getLogger(GuasaAPI.class);

    public static void trackObject(Object obj) {
        if (obj == null || !Guasa.isInitialized()) {
            return;
        }

        try {
            GuasaCore core = Guasa.getCore();
            core.getObjectTracker().trackObject(obj);
        } catch (Exception e) {
            logger.debug("Failed to track object", e);
        }
    }

    public static void untrackObject(Object obj) {
        if (obj == null || !Guasa.isInitialized()) {
            return;
        }

        try {
            GuasaCore core = Guasa.getCore();
            Long objectId = core.getObjectTracker().getObjectId(obj);
            if (objectId != null) {
                core.getObjectTracker().untrackObject(objectId);
            }
        } catch (Exception e) {
            logger.debug("Failed to untrack object", e);
        }
    }

    public static void trackObjectWithTag(Object obj, String tag) {
        if (obj == null || !Guasa.isInitialized()) {
            return;
        }

        try {
            GuasaCore core = Guasa.getCore();
            long id = core.getObjectTracker().trackObject(obj);
            logger.debug("Tracked object with tag '{}': ID {}", tag, id);
        } catch (Exception e) {
            logger.debug("Failed to track object with tag", e);
        }
    }

    public static ObjectInfo getObjectInfo(Object obj) {
        if (obj == null || !Guasa.isInitialized()) {
            return null;
        }

        try {
            GuasaCore core = Guasa.getCore();
            Long objectId = core.getObjectTracker().getObjectId(obj);

            if (objectId == null) {
                return null;
            }

            ObjectTracker.TrackedObject tracked = core.getObjectTracker().getTrackedObject(objectId);
            if (tracked == null) {
                return null;
            }

            long estimatedSize = core.getHeapAnalyzer().estimateObjectSize(obj);
            int referenceCount = core.getReferenceGraph().getReferenceCount(objectId);
            Set<ReferenceGraph.ObjectReference> references = core.getReferenceGraph().getOutgoingReferences(objectId);

            return new ObjectInfo(
                objectId,
                tracked.getClassName(),
                tracked.getIdentityHashCode(),
                estimatedSize,
                referenceCount,
                references.size(),
                tracked.getTrackingTimestamp()
            );

        } catch (Exception e) {
            logger.error("Failed to get object info", e);
            return null;
        }
    }

    public static List<Object> getTrackedObjectsByClass(Class<?> clazz) {
        if (clazz == null || !Guasa.isInitialized()) {
            return Collections.emptyList();
        }

        try {
            GuasaCore core = Guasa.getCore();
            Collection<ObjectTracker.TrackedObject> allTracked = core.getObjectTracker().getAllTrackedObjects();

            List<Object> result = new ArrayList<>();
            String targetClassName = clazz.getName();

            for (ObjectTracker.TrackedObject tracked : allTracked) {
                if (tracked.isAlive() && tracked.getClassName().equals(targetClassName)) {
                    Object obj = tracked.getObject();
                    if (obj != null) {
                        result.add(obj);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            logger.error("Failed to get tracked objects by class", e);
            return Collections.emptyList();
        }
    }

    public static int getTrackedObjectCount() {
        if (!Guasa.isInitialized()) {
            return 0;
        }

        try {
            GuasaCore core = Guasa.getCore();
            return core.getObjectTracker().getTrackedObjectCount();
        } catch (Exception e) {
            logger.error("Failed to get tracked object count", e);
            return 0;
        }
    }

    public static void clearAllTracking() {
        if (!Guasa.isInitialized()) {
            return;
        }

        try {
            GuasaCore core = Guasa.getCore();
            core.getObjectTracker().clearAllTracking();
            core.getReferenceGraph().clear();
            logger.info("All tracking data cleared via API");
        } catch (Exception e) {
            logger.error("Failed to clear tracking", e);
        }
    }

    public static void takeSnapshot() {
        if (!Guasa.isInitialized()) {
            return;
        }

        try {
            GuasaCore core = Guasa.getCore();
            core.getHeapAnalyzer().analyze();
            core.getReferenceGraph().update();
            logger.info("Snapshot taken via API");
        } catch (Exception e) {
            logger.error("Failed to take snapshot", e);
        }
    }

    public static MemoryStats getMemoryStats() {
        if (!Guasa.isInitialized()) {
            return new MemoryStats(0, 0, 0, 0);
        }

        try {
            GuasaCore core = Guasa.getCore();
            var snapshot = core.getHeapAnalyzer().getLastSnapshot();

            if (snapshot == null) {
                return new MemoryStats(0, 0, 0, 0);
            }

            return new MemoryStats(
                snapshot.getHeapUsed(),
                snapshot.getHeapMax(),
                snapshot.getTrackedObjectCount(),
                snapshot.getTotalTrackedMemory()
            );

        } catch (Exception e) {
            logger.error("Failed to get memory stats", e);
            return new MemoryStats(0, 0, 0, 0);
        }
    }

    public static class ObjectInfo {
        private final long id;
        private final String className;
        private final int identityHashCode;
        private final long estimatedSize;
        private final int incomingReferences;
        private final int outgoingReferences;
        private final long trackingTimestamp;

        public ObjectInfo(long id, String className, int identityHashCode, long estimatedSize,
                         int incomingReferences, int outgoingReferences, long trackingTimestamp) {
            this.id = id;
            this.className = className;
            this.identityHashCode = identityHashCode;
            this.estimatedSize = estimatedSize;
            this.incomingReferences = incomingReferences;
            this.outgoingReferences = outgoingReferences;
            this.trackingTimestamp = trackingTimestamp;
        }

        public long getId() { return id; }
        public String getClassName() { return className; }
        public int getIdentityHashCode() { return identityHashCode; }
        public long getEstimatedSize() { return estimatedSize; }
        public int getIncomingReferences() { return incomingReferences; }
        public int getOutgoingReferences() { return outgoingReferences; }
        public long getTrackingTimestamp() { return trackingTimestamp; }
    }

    public static class MemoryStats {
        private final long heapUsed;
        private final long heapMax;
        private final int trackedObjects;
        private final long trackedMemory;

        public MemoryStats(long heapUsed, long heapMax, int trackedObjects, long trackedMemory) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.trackedObjects = trackedObjects;
            this.trackedMemory = trackedMemory;
        }

        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public int getTrackedObjects() { return trackedObjects; }
        public long getTrackedMemory() { return trackedMemory; }
        public double getHeapUsagePercent() {
            return heapMax > 0 ? (heapUsed * 100.0 / heapMax) : 0;
        }
    }
}
