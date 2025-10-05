package com.gdkteam.guasa_synload;

public class StackFrame {
    private final String className;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;

    public StackFrame(String className, String methodName, String fileName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public static StackFrame fromStackTraceElement(StackTraceElement element) {
        return new StackFrame(
            element.getClassName(),
            element.getMethodName(),
            element.getFileName(),
            element.getLineNumber()
        );
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSimpleClassName() {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    @Override
    public String toString() {
        return String.format("%s.%s(%s:%d)",
            className, methodName, fileName, lineNumber);
    }

    public String toFormattedString() {
        return String.format("  at %s.%s(%s:%d)",
            className, methodName, fileName != null ? fileName : "Unknown Source", lineNumber);
    }
}
