/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GuasaConfiguration {
    private static final String DEFAULT_CONFIG_FILE = "guasa.properties";

    private int schedulerThreads = 2;
    private int workerThreads = 4;
    private long updateIntervalMs = 1000;
    private int maxTrackedObjects = 100000;
    private boolean enableAutoTracking = true;
    private boolean enablePythonIntegration = true;
    private int guiRefreshRateMs = 500;
    private boolean enableMemoryProfiling = true;
    private boolean enableReferenceTracking = true;

    public static GuasaConfiguration getDefault() {
        GuasaConfiguration config = new GuasaConfiguration();
        config.loadFromProperties();
        return config;
    }

    private void loadFromProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);

                schedulerThreads = Integer.parseInt(props.getProperty("scheduler.threads", "2"));
                workerThreads = Integer.parseInt(props.getProperty("worker.threads", "4"));
                updateIntervalMs = Long.parseLong(props.getProperty("update.interval.ms", "1000"));
                maxTrackedObjects = Integer.parseInt(props.getProperty("max.tracked.objects", "100000"));
                enableAutoTracking = Boolean.parseBoolean(props.getProperty("enable.auto.tracking", "true"));
                enablePythonIntegration = Boolean.parseBoolean(props.getProperty("enable.python.integration", "true"));
                guiRefreshRateMs = Integer.parseInt(props.getProperty("gui.refresh.rate.ms", "500"));
                enableMemoryProfiling = Boolean.parseBoolean(props.getProperty("enable.memory.profiling", "true"));
                enableReferenceTracking = Boolean.parseBoolean(props.getProperty("enable.reference.tracking", "true"));
            }
        } catch (IOException e) {
            // Use defaults
        }
    }

    public int getSchedulerThreads() { return schedulerThreads; }
    public void setSchedulerThreads(int schedulerThreads) { this.schedulerThreads = schedulerThreads; }

    public int getWorkerThreads() { return workerThreads; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }

    public long getUpdateIntervalMs() { return updateIntervalMs; }
    public void setUpdateIntervalMs(long updateIntervalMs) { this.updateIntervalMs = updateIntervalMs; }

    public int getMaxTrackedObjects() { return maxTrackedObjects; }
    public void setMaxTrackedObjects(int maxTrackedObjects) { this.maxTrackedObjects = maxTrackedObjects; }

    public boolean isEnableAutoTracking() { return enableAutoTracking; }
    public void setEnableAutoTracking(boolean enableAutoTracking) { this.enableAutoTracking = enableAutoTracking; }

    public boolean isEnablePythonIntegration() { return enablePythonIntegration; }
    public void setEnablePythonIntegration(boolean enablePythonIntegration) {
        this.enablePythonIntegration = enablePythonIntegration;
    }

    public int getGuiRefreshRateMs() { return guiRefreshRateMs; }
    public void setGuiRefreshRateMs(int guiRefreshRateMs) { this.guiRefreshRateMs = guiRefreshRateMs; }

    public boolean isEnableMemoryProfiling() { return enableMemoryProfiling; }
    public void setEnableMemoryProfiling(boolean enableMemoryProfiling) {
        this.enableMemoryProfiling = enableMemoryProfiling;
    }

    public boolean isEnableReferenceTracking() { return enableReferenceTracking; }
    public void setEnableReferenceTracking(boolean enableReferenceTracking) {
        this.enableReferenceTracking = enableReferenceTracking;
    }

    @Override
    public String toString() {
        return "GuasaConfiguration{" +
               "schedulerThreads=" + schedulerThreads +
               ", workerThreads=" + workerThreads +
               ", updateIntervalMs=" + updateIntervalMs +
               ", maxTrackedObjects=" + maxTrackedObjects +
               ", enableAutoTracking=" + enableAutoTracking +
               ", enablePythonIntegration=" + enablePythonIntegration +
               ", guiRefreshRateMs=" + guiRefreshRateMs +
               ", enableMemoryProfiling=" + enableMemoryProfiling +
               ", enableReferenceTracking=" + enableReferenceTracking +
               '}';
    }
}
