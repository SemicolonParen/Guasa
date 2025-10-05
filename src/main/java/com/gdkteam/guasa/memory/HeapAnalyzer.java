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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HeapAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(HeapAnalyzer.class);

    private final ObjectTracker objectTracker;
    private final EventBus eventBus;
    private final MemoryMXBean memoryMXBean;
    private final Map<String, ClassStatistics> classStats;
    private volatile boolean analyzing = false;
    private volatile HeapSnapshot lastSnapshot;

    public HeapAnalyzer(ObjectTracker objectTracker, EventBus eventBus) {
        this.objectTracker = objectTracker;
        this.eventBus = eventBus;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.classStats = new ConcurrentHashMap<>();
    }

    public void start() {
        analyzing = true;
        logger.info("HeapAnalyzer started");
    }

    public void stop() {
        analyzing = false;
        logger.info("HeapAnalyzer stopped");
    }

    public void analyze() {
        if (!analyzing) {
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            Collection<ObjectTracker.TrackedObject> trackedObjects = objectTracker.getAllTrackedObjects();

            Map<String, Integer> objectCountByClass = new HashMap<>();
            Map<String, Long> memorySizeByClass = new HashMap<>();
            long totalTrackedMemory = 0;

            for (ObjectTracker.TrackedObject tracked : trackedObjects) {
                Object obj = tracked.getObject();
                if (obj != null) {
                    String className = obj.getClass().getName();
                    objectCountByClass.merge(className, 1, Integer::sum);

                    long objSize = estimateObjectSize(obj);
                    memorySizeByClass.merge(className, objSize, Long::sum);
                    totalTrackedMemory += objSize;

                    updateClassStatistics(className, objSize);
                }
            }

            lastSnapshot = new HeapSnapshot(
                heapUsage.getUsed(),
                heapUsage.getMax(),
                heapUsage.getCommitted(),
                nonHeapUsage.getUsed(),
                trackedObjects.size(),
                totalTrackedMemory,
                objectCountByClass,
                memorySizeByClass,
                System.currentTimeMillis()
            );

            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Heap analysis completed in {}ms", duration);

            eventBus.publish(new GuasaEvent.HeapAnalyzed(lastSnapshot));

        } catch (Exception e) {
            logger.error("Error during heap analysis", e);
        }
    }

    public long estimateObjectSize(Object obj) {
        if (obj == null) {
            return 0;
        }

        Class<?> clazz = obj.getClass();
        long size = 16;

        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            Class<?> componentType = clazz.getComponentType();

            if (componentType.isPrimitive()) {
                if (componentType == boolean.class || componentType == byte.class) {
                    size += length;
                } else if (componentType == char.class || componentType == short.class) {
                    size += length * 2L;
                } else if (componentType == int.class || componentType == float.class) {
                    size += length * 4L;
                } else if (componentType == long.class || componentType == double.class) {
                    size += length * 8L;
                }
            } else {
                size += length * 4L;
            }
        } else {
            size += estimateFieldsSize(clazz);
        }

        return alignTo8Bytes(size);
    }

    private long estimateFieldsSize(Class<?> clazz) {
        long size = 0;

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                Class<?> fieldType = field.getType();
                if (fieldType.isPrimitive()) {
                    if (fieldType == boolean.class || fieldType == byte.class) {
                        size += 1;
                    } else if (fieldType == char.class || fieldType == short.class) {
                        size += 2;
                    } else if (fieldType == int.class || fieldType == float.class) {
                        size += 4;
                    } else if (fieldType == long.class || fieldType == double.class) {
                        size += 8;
                    }
                } else {
                    size += 4;
                }
            }
            clazz = clazz.getSuperclass();
        }

        return size;
    }

    private long alignTo8Bytes(long size) {
        return ((size + 7) / 8) * 8;
    }

    private void updateClassStatistics(String className, long size) {
        classStats.compute(className, (k, stats) -> {
            if (stats == null) {
                stats = new ClassStatistics(className);
            }
            stats.incrementCount();
            stats.addMemorySize(size);
            return stats;
        });
    }

    public HeapSnapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public Map<String, ClassStatistics> getClassStatistics() {
        return new HashMap<>(classStats);
    }

    public void clearStatistics() {
        classStats.clear();
    }

    public static class HeapSnapshot {
        private final long heapUsed;
        private final long heapMax;
        private final long heapCommitted;
        private final long nonHeapUsed;
        private final int trackedObjectCount;
        private final long totalTrackedMemory;
        private final Map<String, Integer> objectCountByClass;
        private final Map<String, Long> memorySizeByClass;
        private final long timestamp;

        public HeapSnapshot(long heapUsed, long heapMax, long heapCommitted, long nonHeapUsed,
                          int trackedObjectCount, long totalTrackedMemory,
                          Map<String, Integer> objectCountByClass, Map<String, Long> memorySizeByClass,
                          long timestamp) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.heapCommitted = heapCommitted;
            this.nonHeapUsed = nonHeapUsed;
            this.trackedObjectCount = trackedObjectCount;
            this.totalTrackedMemory = totalTrackedMemory;
            this.objectCountByClass = objectCountByClass;
            this.memorySizeByClass = memorySizeByClass;
            this.timestamp = timestamp;
        }

        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getHeapCommitted() { return heapCommitted; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public int getTrackedObjectCount() { return trackedObjectCount; }
        public long getTotalTrackedMemory() { return totalTrackedMemory; }
        public Map<String, Integer> getObjectCountByClass() { return objectCountByClass; }
        public Map<String, Long> getMemorySizeByClass() { return memorySizeByClass; }
        public long getTimestamp() { return timestamp; }
        public double getHeapUsagePercentage() {
            return heapMax > 0 ? (heapUsed * 100.0 / heapMax) : 0;
        }
    }

    public static class ClassStatistics {
        private final String className;
        private int instanceCount;
        private long totalMemorySize;
        private long lastUpdateTime;

        public ClassStatistics(String className) {
            this.className = className;
            this.instanceCount = 0;
            this.totalMemorySize = 0;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void incrementCount() {
            instanceCount++;
            lastUpdateTime = System.currentTimeMillis();
        }

        public void addMemorySize(long size) {
            totalMemorySize += size;
            lastUpdateTime = System.currentTimeMillis();
        }

        public String getClassName() { return className; }
        public int getInstanceCount() { return instanceCount; }
        public long getTotalMemorySize() { return totalMemorySize; }
        public long getLastUpdateTime() { return lastUpdateTime; }
        public long getAverageSize() {
            return instanceCount > 0 ? totalMemorySize / instanceCount : 0;
        }
    }
}
