package com.gdkteam.guasa_synload;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=== GUASA SynLoad Demo ===\n");

        demonstrateSyntaxHighlighting();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateBreakpointManager();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateStackTraceAnalyzer();
    }

    private static void demonstrateSyntaxHighlighting() {
        System.out.println("1. Syntax Highlighting Demo");
        System.out.println("-".repeat(60));

        String javaCode = "public class Hello {\n" +
                          "    // This is a comment\n" +
                          "    public static void main(String[] args) {\n" +
                          "        int num = 42;\n" +
                          "        String msg = \"Hello, World!\";\n" +
                          "        System.out.println(msg);\n" +
                          "    }\n" +
                          "}";

        Language java = LanguageRegistry.getLanguage("java");
        SyntaxHighlighter javaHighlighter = new SyntaxHighlighter(java);

        System.out.println("\nOriginal Java Code:");
        System.out.println(javaCode);

        System.out.println("\n\nHighlighted Java Code:");
        String highlighted = javaHighlighter.highlight(javaCode);
        System.out.println(highlighted);

        System.out.println("\n\nTokenization:");
        List<Token> tokens = javaHighlighter.tokenize(javaCode);
        tokens.stream()
            .filter(t -> t.getType() != TokenType.WHITESPACE)
            .limit(15)
            .forEach(System.out::println);
        System.out.println("... (showing first 15 non-whitespace tokens)");

        String pythonCode = "def greet(name):\n" +
                           "    # Print greeting\n" +
                           "    return f\"Hello, {name}!\"";

        Language python = LanguageRegistry.getLanguage("python");
        SyntaxHighlighter pythonHighlighter = new SyntaxHighlighter(python);

        System.out.println("\n\nPython Code (Highlighted):");
        System.out.println(pythonHighlighter.highlight(pythonCode));

        System.out.println("\n\nSupported Languages: " + LanguageRegistry.getSupportedLanguages());
    }

    private static void demonstrateBreakpointManager() {
        System.out.println("2. Breakpoint Manager Demo");
        System.out.println("-".repeat(60));

        BreakpointManager bpm = new BreakpointManager();

        bpm.addBreakpoint("Main.java", 15);
        bpm.addBreakpoint("Main.java", 23);
        bpm.addBreakpoint("Utils.java", 42, "x > 100");
        bpm.addBreakpoint("Controller.java", 78);

        System.out.println("\nAdded 4 breakpoints:");
        bpm.printBreakpoints();

        System.out.println("\n\nChecking breakpoint at Main.java:15: " +
            bpm.hasBreakpoint("Main.java", 15));

        System.out.println("\nToggling breakpoint at Main.java:15...");
        bpm.toggleBreakpoint("Main.java", 15);

        System.out.println("\nAfter toggle:");
        bpm.printBreakpoints();

        System.out.println("\n\nRemoving breakpoint at Controller.java:78...");
        bpm.removeBreakpoint("Controller.java", 78);

        System.out.println("Total breakpoints: " + bpm.getBreakpointCount());
        bpm.printBreakpoints();
    }

    private static void demonstrateStackTraceAnalyzer() {
        System.out.println("3. Stack Trace Analyzer Demo");
        System.out.println("-".repeat(60));

        try {
            methodA();
        } catch (Exception e) {
            System.out.println("\nCaught exception! Analyzing stack trace...\n");
            StackTraceAnalyzer.printAnalysis(e);

            List<StackFrame> frames = StackTraceAnalyzer.analyze(e);
            System.out.println("\n\nStack depth: " + StackTraceAnalyzer.getStackDepth(e));

            List<StackFrame> userFrames = StackTraceAnalyzer.filterByPackage(
                frames, "com.gdkteam.guasa_synload");
            System.out.println("\nUser code frames:");
            userFrames.forEach(frame ->
                System.out.println("  " + frame.toFormattedString()));

            System.out.println("\n\nUnique packages in stack:");
            List<String> packages = StackTraceAnalyzer.getUniquePackages(frames);
            packages.forEach(pkg -> System.out.println("  - " + pkg));
        }
    }

    private static void methodA() throws Exception {
        methodB();
    }

    private static void methodB() throws Exception {
        methodC();
    }

    private static void methodC() throws Exception {
        throw new RuntimeException("Simulated error for demo purposes");
    }
}
