package com.gdkteam.guasa_synload;

public class Breakpoint {
    private final String fileName;
    private final int lineNumber;
    private boolean enabled;
    private String condition;

    public Breakpoint(String fileName, int lineNumber) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.enabled = true;
        this.condition = null;
    }

    public Breakpoint(String fileName, int lineNumber, String condition) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.enabled = true;
        this.condition = condition;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean hasCondition() {
        return condition != null && !condition.isEmpty();
    }

    @Override
    public String toString() {
        String status = enabled ? "ENABLED" : "DISABLED";
        String condStr = hasCondition() ? " [" + condition + "]" : "";
        return String.format("Breakpoint{%s:%d %s%s}", fileName, lineNumber, status, condStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Breakpoint that = (Breakpoint) o;
        return lineNumber == that.lineNumber && fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return 31 * fileName.hashCode() + lineNumber;
    }
}
