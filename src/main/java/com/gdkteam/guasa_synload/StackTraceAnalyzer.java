package com.gdkteam.guasa_synload;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StackTraceAnalyzer {

    public static List<StackFrame> analyze(Throwable throwable) {
        List<StackFrame> frames = new ArrayList<>();
        for (StackTraceElement element : throwable.getStackTrace()) {
            frames.add(StackFrame.fromStackTraceElement(element));
        }
        return frames;
    }

    public static String formatStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(throwable.getMessage());
        }
        sb.append("\n");

        List<StackFrame> frames = analyze(throwable);
        for (StackFrame frame : frames) {
            sb.append(frame.toFormattedString()).append("\n");
        }

        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(formatStackTrace(throwable.getCause()));
        }

        return sb.toString();
    }

    public static List<StackFrame> filterByPackage(List<StackFrame> frames, String packagePrefix) {
        return frames.stream()
            .filter(frame -> frame.getClassName().startsWith(packagePrefix))
            .collect(Collectors.toList());
    }

    public static StackFrame findFirstUserFrame(List<StackFrame> frames, String userPackagePrefix) {
        return frames.stream()
            .filter(frame -> frame.getClassName().startsWith(userPackagePrefix))
            .findFirst()
            .orElse(null);
    }

    public static void printAnalysis(Throwable throwable) {
        System.out.println("=== Stack Trace Analysis ===");
        System.out.println("Exception Type: " + throwable.getClass().getSimpleName());
        System.out.println("Message: " + throwable.getMessage());
        System.out.println("\nStack Frames:");

        List<StackFrame> frames = analyze(throwable);
        for (int i = 0; i < frames.size(); i++) {
            StackFrame frame = frames.get(i);
            System.out.printf("[%d] %s\n", i, frame.toFormattedString());
        }

        if (throwable.getCause() != null) {
            System.out.println("\n=== Caused By ===");
            printAnalysis(throwable.getCause());
        }
    }

    public static int getStackDepth(Throwable throwable) {
        return throwable.getStackTrace().length;
    }

    public static List<String> getUniquePackages(List<StackFrame> frames) {
        return frames.stream()
            .map(frame -> {
                int lastDot = frame.getClassName().lastIndexOf('.');
                return lastDot >= 0 ? frame.getClassName().substring(0, lastDot) : "";
            })
            .distinct()
            .collect(Collectors.toList());
    }
}
