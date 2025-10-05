/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.gui.GuasaMainWindow;
import com.gdkteam.guasa.config.GuasaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Guasa {
    private static final Logger logger = LoggerFactory.getLogger(Guasa.class);
    private static GuasaCore core;
    private static GuasaMainWindow mainWindow;
    private static volatile boolean initialized = false;

    public static void main(String[] args) {
        initialize();
    }

    public static synchronized void initialize() {
        if (initialized) {
            logger.warn("Guasa already initialized");
            return;
        }

        try {
            logger.info("Initializing Guasa Visual Debugger v1.0.0");

            GuasaConfiguration config = GuasaConfiguration.getDefault();
            core = new GuasaCore(config);
            core.start();

            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    logger.warn("Failed to set system look and feel", e);
                }

                mainWindow = new GuasaMainWindow(core);
                mainWindow.setVisible(true);
            });

            initialized = true;
            logger.info("Guasa initialization complete");

        } catch (Exception e) {
            logger.error("Failed to initialize Guasa", e);
            throw new RuntimeException("Guasa initialization failed", e);
        }
    }

    public static GuasaCore getCore() {
        if (!initialized) {
            throw new IllegalStateException("Guasa not initialized");
        }
        return core;
    }

    public static GuasaMainWindow getMainWindow() {
        return mainWindow;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void shutdown() {
        if (!initialized) {
            return;
        }

        logger.info("Shutting down Guasa");

        if (mainWindow != null) {
            mainWindow.dispose();
        }

        if (core != null) {
            core.stop();
        }

        initialized = false;
        logger.info("Guasa shutdown complete");
    }
}
