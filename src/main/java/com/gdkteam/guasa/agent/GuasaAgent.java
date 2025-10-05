/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.agent;

import com.gdkteam.guasa.Guasa;
import com.gdkteam.guasa.instrumentation.ClassTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class GuasaAgent {
    private static final Logger logger = LoggerFactory.getLogger(GuasaAgent.class);
    private static Instrumentation instrumentation;
    private static volatile boolean agentLoaded = false;

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Guasa Agent starting (premain)");
        initializeAgent(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.info("Guasa Agent starting (agentmain)");
        initializeAgent(agentArgs, inst);
    }

    private static void initializeAgent(String agentArgs, Instrumentation inst) {
        if (agentLoaded) {
            logger.warn("Guasa Agent already loaded");
            return;
        }

        instrumentation = inst;

        try {
            AgentConfiguration config = AgentConfiguration.parse(agentArgs);

            if (config.isEnableTransformation()) {
                ClassTransformer transformer = new ClassTransformer(config);
                inst.addTransformer(transformer, inst.isRetransformClassesSupported());
                logger.info("Class transformer registered");
            }

            if (config.isAutoStartGui()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Guasa.initialize();
                    } catch (Exception e) {
                        logger.error("Failed to auto-start Guasa GUI", e);
                    }
                }).start();
            }

            agentLoaded = true;
            logger.info("Guasa Agent initialized successfully with config: {}", config);

        } catch (Exception e) {
            logger.error("Failed to initialize Guasa Agent", e);
        }
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isAgentLoaded() {
        return agentLoaded;
    }
}
