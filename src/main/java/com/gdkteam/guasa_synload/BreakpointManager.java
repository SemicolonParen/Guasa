package com.gdkteam.guasa_synload;

import java.util.*;

public class BreakpointManager {
    private final Map<String, List<Breakpoint>> breakpoints;

    public BreakpointManager() {
        this.breakpoints = new HashMap<>();
    }

    public void addBreakpoint(String fileName, int lineNumber) {
        Breakpoint bp = new Breakpoint(fileName, lineNumber);
        breakpoints.computeIfAbsent(fileName, k -> new ArrayList<>()).add(bp);
    }

    public void addBreakpoint(String fileName, int lineNumber, String condition) {
        Breakpoint bp = new Breakpoint(fileName, lineNumber, condition);
        breakpoints.computeIfAbsent(fileName, k -> new ArrayList<>()).add(bp);
    }

    public boolean removeBreakpoint(String fileName, int lineNumber) {
        List<Breakpoint> fileBreakpoints = breakpoints.get(fileName);
        if (fileBreakpoints != null) {
            return fileBreakpoints.removeIf(bp -> bp.getLineNumber() == lineNumber);
        }
        return false;
    }

    public void clearBreakpoints(String fileName) {
        breakpoints.remove(fileName);
    }

    public void clearAllBreakpoints() {
        breakpoints.clear();
    }

    public List<Breakpoint> getBreakpoints(String fileName) {
        return breakpoints.getOrDefault(fileName, Collections.emptyList());
    }

    public List<Breakpoint> getAllBreakpoints() {
        List<Breakpoint> allBreakpoints = new ArrayList<>();
        for (List<Breakpoint> bps : breakpoints.values()) {
            allBreakpoints.addAll(bps);
        }
        return allBreakpoints;
    }

    public boolean hasBreakpoint(String fileName, int lineNumber) {
        List<Breakpoint> fileBreakpoints = breakpoints.get(fileName);
        if (fileBreakpoints != null) {
            return fileBreakpoints.stream()
                .anyMatch(bp -> bp.getLineNumber() == lineNumber && bp.isEnabled());
        }
        return false;
    }

    public void toggleBreakpoint(String fileName, int lineNumber) {
        List<Breakpoint> fileBreakpoints = breakpoints.get(fileName);
        if (fileBreakpoints != null) {
            fileBreakpoints.stream()
                .filter(bp -> bp.getLineNumber() == lineNumber)
                .findFirst()
                .ifPresent(bp -> bp.setEnabled(!bp.isEnabled()));
        }
    }

    public int getBreakpointCount() {
        return breakpoints.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    public void printBreakpoints() {
        if (breakpoints.isEmpty()) {
            System.out.println("No breakpoints set.");
            return;
        }

        System.out.println("Active Breakpoints:");
        breakpoints.forEach((file, bps) -> {
            System.out.println("  " + file + ":");
            bps.forEach(bp -> System.out.println("    " + bp));
        });
    }
}
