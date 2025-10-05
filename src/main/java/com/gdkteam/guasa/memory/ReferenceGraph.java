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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReferenceGraph {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceGraph.class);

    private final ObjectTracker objectTracker;
    private final EventBus eventBus;
    private final Map<Long, Set<ObjectReference>> outgoingReferences;
    private final Map<Long, Set<Long>> incomingReferences;
    private volatile boolean active = false;

    public ReferenceGraph(ObjectTracker objectTracker, EventBus eventBus) {
        this.objectTracker = objectTracker;
        this.eventBus = eventBus;
        this.outgoingReferences = new ConcurrentHashMap<>();
        this.incomingReferences = new ConcurrentHashMap<>();
    }

    public void start() {
        active = true;
        logger.info("ReferenceGraph started");
    }

    public void stop() {
        active = false;
        logger.info("ReferenceGraph stopped");
    }

    public void update() {
        if (!active) {
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            outgoingReferences.clear();
            incomingReferences.clear();

            Collection<ObjectTracker.TrackedObject> trackedObjects = objectTracker.getAllTrackedObjects();

            for (ObjectTracker.TrackedObject tracked : trackedObjects) {
                Object obj = tracked.getObject();
                if (obj != null) {
                    analyzeObjectReferences(tracked.getId(), obj);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Reference graph updated in {}ms", duration);

            eventBus.publish(new GuasaEvent.ReferenceGraphUpdated());

        } catch (Exception e) {
            logger.error("Error updating reference graph", e);
        }
    }

    private void analyzeObjectReferences(long objectId, Object obj) {
        Set<ObjectReference> references = new HashSet<>();

        try {
            if (obj.getClass().isArray()) {
                analyzeArrayReferences(objectId, obj, references);
            } else {
                analyzeObjectFieldReferences(objectId, obj, references);
            }

            outgoingReferences.put(objectId, references);

            for (ObjectReference ref : references) {
                incomingReferences.computeIfAbsent(ref.getTargetId(), k -> ConcurrentHashMap.newKeySet())
                    .add(objectId);
            }

        } catch (Exception e) {
            logger.warn("Error analyzing references for object {}", objectId, e);
        }
    }

    private void analyzeArrayReferences(long objectId, Object array, Set<ObjectReference> references) {
        Class<?> componentType = array.getClass().getComponentType();

        if (componentType.isPrimitive()) {
            return;
        }

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            if (element != null) {
                Long targetId = objectTracker.getObjectId(element);
                if (targetId != null) {
                    references.add(new ObjectReference(
                        objectId,
                        targetId,
                        "[" + i + "]",
                        ReferenceType.ARRAY_ELEMENT,
                        element.getClass().getName()
                    ));
                }
            }
        }
    }

    private void analyzeObjectFieldReferences(long objectId, Object obj, Set<ObjectReference> references) {
        Class<?> clazz = obj.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);

                    if (fieldValue != null) {
                        Long targetId = objectTracker.getObjectId(fieldValue);
                        if (targetId != null) {
                            references.add(new ObjectReference(
                                objectId,
                                targetId,
                                field.getName(),
                                ReferenceType.FIELD,
                                fieldValue.getClass().getName()
                            ));
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Cannot access field {} of object {}", field.getName(), objectId);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public Set<ObjectReference> getOutgoingReferences(long objectId) {
        return outgoingReferences.getOrDefault(objectId, Collections.emptySet());
    }

    public Set<Long> getIncomingReferences(long objectId) {
        return incomingReferences.getOrDefault(objectId, Collections.emptySet());
    }

    public List<Long> findPathToRoot(long objectId) {
        List<Long> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        if (findPathToRootRecursive(objectId, path, visited)) {
            Collections.reverse(path);
            return path;
        }

        return Collections.emptyList();
    }

    private boolean findPathToRootRecursive(long objectId, List<Long> path, Set<Long> visited) {
        if (visited.contains(objectId)) {
            return false;
        }

        visited.add(objectId);
        path.add(objectId);

        Set<Long> incoming = getIncomingReferences(objectId);

        if (incoming.isEmpty()) {
            return true;
        }

        for (Long parentId : incoming) {
            if (findPathToRootRecursive(parentId, path, visited)) {
                return true;
            }
        }

        path.remove(path.size() - 1);
        return false;
    }

    public int getReferenceCount(long objectId) {
        return getIncomingReferences(objectId).size();
    }

    public Map<Long, Set<ObjectReference>> getFullGraph() {
        return new HashMap<>(outgoingReferences);
    }

    public void clear() {
        outgoingReferences.clear();
        incomingReferences.clear();
    }

    public enum ReferenceType {
        FIELD,
        ARRAY_ELEMENT,
        COLLECTION_ELEMENT,
        STATIC_FIELD
    }

    public static class ObjectReference {
        private final long sourceId;
        private final long targetId;
        private final String fieldName;
        private final ReferenceType type;
        private final String targetClassName;

        public ObjectReference(long sourceId, long targetId, String fieldName,
                             ReferenceType type, String targetClassName) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.fieldName = fieldName;
            this.type = type;
            this.targetClassName = targetClassName;
        }

        public long getSourceId() { return sourceId; }
        public long getTargetId() { return targetId; }
        public String getFieldName() { return fieldName; }
        public ReferenceType getType() { return type; }
        public String getTargetClassName() { return targetClassName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectReference that = (ObjectReference) o;
            return sourceId == that.sourceId && targetId == that.targetId &&
                   Objects.equals(fieldName, that.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceId, targetId, fieldName);
        }
    }
}
