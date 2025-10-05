/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private final Map<Class<? extends GuasaEvent>, List<EventListener<?>>> listeners;
    private final ExecutorService asyncExecutor;
    private volatile boolean active = true;

    public EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("EventBus-Worker-" + t.getId());
                return t;
            }
        );
    }

    public <T extends GuasaEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.debug("Subscribed listener for event type: {}", eventType.getSimpleName());
    }

    public <T extends GuasaEvent> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            logger.debug("Unsubscribed listener for event type: {}", eventType.getSimpleName());
        }
    }

    public void publish(GuasaEvent event) {
        if (!active) {
            return;
        }

        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }

        for (EventListener<?> listener : eventListeners) {
            try {
                @SuppressWarnings("unchecked")
                EventListener<GuasaEvent> typedListener = (EventListener<GuasaEvent>) listener;
                typedListener.onEvent(event);
            } catch (Exception e) {
                logger.error("Error notifying listener for event: {}", event.getClass().getSimpleName(), e);
            }
        }
    }

    public void publishAsync(GuasaEvent event) {
        if (!active) {
            return;
        }

        asyncExecutor.execute(() -> publish(event));
    }

    public void shutdown() {
        active = false;
        asyncExecutor.shutdown();
    }

    @FunctionalInterface
    public interface EventListener<T extends GuasaEvent> {
        void onEvent(T event);
    }
}
