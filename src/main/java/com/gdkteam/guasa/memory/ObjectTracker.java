/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.memory;

import com.gdkteam.guasa.event.EventBus;
import com.gdkteam.guasa.event.GuasaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectTracker {
    private static final Logger logger = LoggerFactory.getLogger(ObjectTracker.class);

    private final Map<Long, TrackedObject> trackedObjects;
    private final Map<Object, Long> objectToIdMap;
    private final AtomicLong idGenerator;
    private final EventBus eventBus;
    private volatile boolean tracking = false;

    public ObjectTracker(EventBus eventBus) {
        this.trackedObjects = new ConcurrentHashMap<>();
        this.objectToIdMap = Collections.synchronizedMap(new WeakHashMap<>());
        this.idGenerator = new AtomicLong(1);
        this.eventBus = eventBus;
    }

    public void start() {
        if (tracking) {
            return;
        }
        tracking = true;
        logger.info("ObjectTracker started");
    }

    public void stop() {
        tracking = false;
        logger.info("ObjectTracker stopped");
    }

    public long trackObject(Object obj) {
        if (obj == null || !tracking) {
            return -1;
        }

        Long existingId = objectToIdMap.get(obj);
        if (existingId != null) {
            return existingId;
        }

        long id = idGenerator.getAndIncrement();
        TrackedObject tracked = new TrackedObject(id, obj);

        trackedObjects.put(id, tracked);
        objectToIdMap.put(obj, id);

        eventBus.publish(new GuasaEvent.ObjectTracked(id, obj.getClass().getName()));

        return id;
    }

    public void untrackObject(long id) {
        TrackedObject tracked = trackedObjects.remove(id);
        if (tracked != null && tracked.getObject() != null) {
            objectToIdMap.remove(tracked.getObject());
            eventBus.publish(new GuasaEvent.ObjectUntracked(id));
        }
    }

    public TrackedObject getTrackedObject(long id) {
        return trackedObjects.get(id);
    }

    public Long getObjectId(Object obj) {
        return objectToIdMap.get(obj);
    }

    public Collection<TrackedObject> getAllTrackedObjects() {
        cleanupStaleReferences();
        return new ArrayList<>(trackedObjects.values());
    }

    public int getTrackedObjectCount() {
        return trackedObjects.size();
    }

    public void clearAllTracking() {
        trackedObjects.clear();
        objectToIdMap.clear();
        idGenerator.set(1);
        logger.info("Cleared all tracked objects");
    }

    private void cleanupStaleReferences() {
        List<Long> staleIds = new ArrayList<>();

        for (Map.Entry<Long, TrackedObject> entry : trackedObjects.entrySet()) {
            if (entry.getValue().getObject() == null) {
                staleIds.add(entry.getKey());
            }
        }

        for (Long id : staleIds) {
            trackedObjects.remove(id);
        }

        if (!staleIds.isEmpty()) {
            logger.debug("Cleaned up {} stale object references", staleIds.size());
        }
    }

    public static class TrackedObject {
        private final long id;
        private final WeakReference<Object> objectRef;
        private final String className;
        private final long trackingTimestamp;
        private final int identityHashCode;
        private volatile long lastAccessTime;
        private volatile int accessCount;

        public TrackedObject(long id, Object obj) {
            this.id = id;
            this.objectRef = new WeakReference<>(obj);
            this.className = obj.getClass().getName();
            this.trackingTimestamp = System.currentTimeMillis();
            this.identityHashCode = System.identityHashCode(obj);
            this.lastAccessTime = trackingTimestamp;
            this.accessCount = 0;
        }

        public long getId() {
            return id;
        }

        public Object getObject() {
            Object obj = objectRef.get();
            if (obj != null) {
                lastAccessTime = System.currentTimeMillis();
                accessCount++;
            }
            return obj;
        }

        public String getClassName() {
            return className;
        }

        public long getTrackingTimestamp() {
            return trackingTimestamp;
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public int getAccessCount() {
            return accessCount;
        }

        public boolean isAlive() {
            return objectRef.get() != null;
        }
    }
}
