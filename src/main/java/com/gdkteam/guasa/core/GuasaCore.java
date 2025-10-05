/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.core;

import com.gdkteam.guasa.config.GuasaConfiguration;
import com.gdkteam.guasa.event.EventBus;
import com.gdkteam.guasa.event.GuasaEvent;
import com.gdkteam.guasa.memory.HeapAnalyzer;
import com.gdkteam.guasa.memory.ObjectTracker;
import com.gdkteam.guasa.memory.ReferenceGraph;
import com.gdkteam.guasa.python.PythonBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class GuasaCore {
    private static final Logger logger = LoggerFactory.getLogger(GuasaCore.class);

    private final GuasaConfiguration configuration;
    private final ObjectTracker objectTracker;
    private final HeapAnalyzer heapAnalyzer;
    private final ReferenceGraph referenceGraph;
    private final EventBus eventBus;
    private final PythonBridge pythonBridge;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService workerPool;

    private volatile boolean running = false;
    private ScheduledFuture<?> analysisTask;

    public GuasaCore(GuasaConfiguration configuration) {
        this.configuration = configuration;
        this.eventBus = new EventBus();
        this.objectTracker = new ObjectTracker(eventBus);
        this.heapAnalyzer = new HeapAnalyzer(objectTracker, eventBus);
        this.referenceGraph = new ReferenceGraph(objectTracker, eventBus);
        this.pythonBridge = new PythonBridge();

        this.scheduler = Executors.newScheduledThreadPool(
            configuration.getSchedulerThreads(),
            this::createThread
        );

        this.workerPool = Executors.newFixedThreadPool(
            configuration.getWorkerThreads(),
            this::createThread
        );

        logger.info("GuasaCore initialized with configuration: {}", configuration);
    }

    public void start() {
        if (running) {
            logger.warn("GuasaCore already running");
            return;
        }

        logger.info("Starting GuasaCore");

        pythonBridge.initialize();
        objectTracker.start();
        heapAnalyzer.start();
        referenceGraph.start();

        long updateInterval = configuration.getUpdateIntervalMs();
        analysisTask = scheduler.scheduleAtFixedRate(
            this::performAnalysis,
            0,
            updateInterval,
            TimeUnit.MILLISECONDS
        );

        running = true;
        eventBus.publish(new GuasaEvent.CoreStarted());
        logger.info("GuasaCore started successfully");
    }

    public void stop() {
        if (!running) {
            return;
        }

        logger.info("Stopping GuasaCore");
        running = false;

        if (analysisTask != null) {
            analysisTask.cancel(false);
        }

        referenceGraph.stop();
        heapAnalyzer.stop();
        objectTracker.stop();
        pythonBridge.shutdown();

        scheduler.shutdown();
        workerPool.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        eventBus.publish(new GuasaEvent.CoreStopped());
        logger.info("GuasaCore stopped");
    }

    private void performAnalysis() {
        try {
            heapAnalyzer.analyze();
            referenceGraph.update();
            eventBus.publish(new GuasaEvent.AnalysisComplete());
        } catch (Exception e) {
            logger.error("Error during analysis", e);
        }
    }

    private Thread createThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Guasa-Worker-" + thread.getId());
        return thread;
    }

    public ObjectTracker getObjectTracker() {
        return objectTracker;
    }

    public HeapAnalyzer getHeapAnalyzer() {
        return heapAnalyzer;
    }

    public ReferenceGraph getReferenceGraph() {
        return referenceGraph;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public PythonBridge getPythonBridge() {
        return pythonBridge;
    }

    public GuasaConfiguration getConfiguration() {
        return configuration;
    }

    public ExecutorService getWorkerPool() {
        return workerPool;
    }

    public boolean isRunning() {
        return running;
    }
}
