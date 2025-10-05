/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.agent;

import java.util.*;

public class AgentConfiguration {
    private boolean enableTransformation = true;
    private boolean autoStartGui = true;
    private Set<String> includedPackages = new HashSet<>();
    private Set<String> excludedPackages = new HashSet<>();
    private boolean trackAllObjects = false;
    private int samplingRate = 100;

    public AgentConfiguration() {
        excludedPackages.add("java.");
        excludedPackages.add("javax.");
        excludedPackages.add("sun.");
        excludedPackages.add("com.sun.");
        excludedPackages.add("jdk.");
        excludedPackages.add("org.python.");
        excludedPackages.add("com.gdkteam.guasa.");
    }

    public static AgentConfiguration parse(String agentArgs) {
        AgentConfiguration config = new AgentConfiguration();

        if (agentArgs == null || agentArgs.trim().isEmpty()) {
            return config;
        }

        String[] args = agentArgs.split(",");
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "transform":
                    config.enableTransformation = Boolean.parseBoolean(value);
                    break;
                case "autostart":
                    config.autoStartGui = Boolean.parseBoolean(value);
                    break;
                case "include":
                    config.includedPackages.addAll(Arrays.asList(value.split(":")));
                    break;
                case "exclude":
                    config.excludedPackages.addAll(Arrays.asList(value.split(":")));
                    break;
                case "trackAll":
                    config.trackAllObjects = Boolean.parseBoolean(value);
                    break;
                case "sampling":
                    config.samplingRate = Integer.parseInt(value);
                    break;
            }
        }

        return config;
    }

    public boolean shouldTransformClass(String className) {
        if (className == null) {
            return false;
        }

        for (String excluded : excludedPackages) {
            if (className.startsWith(excluded)) {
                return false;
            }
        }

        if (includedPackages.isEmpty()) {
            return true;
        }

        for (String included : includedPackages) {
            if (className.startsWith(included)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEnableTransformation() {
        return enableTransformation;
    }

    public void setEnableTransformation(boolean enableTransformation) {
        this.enableTransformation = enableTransformation;
    }

    public boolean isAutoStartGui() {
        return autoStartGui;
    }

    public void setAutoStartGui(boolean autoStartGui) {
        this.autoStartGui = autoStartGui;
    }

    public Set<String> getIncludedPackages() {
        return includedPackages;
    }

    public Set<String> getExcludedPackages() {
        return excludedPackages;
    }

    public boolean isTrackAllObjects() {
        return trackAllObjects;
    }

    public void setTrackAllObjects(boolean trackAllObjects) {
        this.trackAllObjects = trackAllObjects;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    @Override
    public String toString() {
        return "AgentConfiguration{" +
               "enableTransformation=" + enableTransformation +
               ", autoStartGui=" + autoStartGui +
               ", includedPackages=" + includedPackages +
               ", excludedPackages=" + excludedPackages +
               ", trackAllObjects=" + trackAllObjects +
               ", samplingRate=" + samplingRate +
               '}';
    }
}
