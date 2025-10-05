/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.instrumentation;

import com.gdkteam.guasa.agent.AgentConfiguration;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClassTransformer implements ClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ClassTransformer.class);

    private final AgentConfiguration config;
    private long transformCount = 0;

    public ClassTransformer(AgentConfiguration config) {
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (className == null || !config.shouldTransformClass(className.replace('/', '.'))) {
                return null;
            }

            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

            ObjectTrackingVisitor visitor = new ObjectTrackingVisitor(Opcodes.ASM9, writer, className);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            transformCount++;
            if (transformCount % 100 == 0) {
                logger.debug("Transformed {} classes", transformCount);
            }

            return writer.toByteArray();

        } catch (Exception e) {
            logger.error("Error transforming class: {}", className, e);
            return null;
        }
    }

    public long getTransformCount() {
        return transformCount;
    }

    private static class ObjectTrackingVisitor extends ClassVisitor {
        private final String className;

        public ObjectTrackingVisitor(int api, ClassVisitor cv, String className) {
            super(api, cv);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                        String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if (name.equals("<init>")) {
                return new ConstructorTrackingVisitor(api, mv, className);
            }

            return mv;
        }
    }

    private static class ConstructorTrackingVisitor extends MethodVisitor {
        private final String className;
        private boolean constructorCallFound = false;

        public ConstructorTrackingVisitor(int api, MethodVisitor mv, String className) {
            super(api, mv);
            this.className = className;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

            if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && !constructorCallFound) {
                constructorCallFound = true;

                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/gdkteam/guasa/api/GuasaAPI",
                    "trackObject",
                    "(Ljava/lang/Object;)V",
                    false
                );
            }
        }
    }
}
