package com.gdkteam.guasa_synload;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LanguageRegistry {
    private static final Map<String, Language> languages = new HashMap<>();

    static {
        registerJava();
        registerPython();
        registerJavaScript();
        registerSQL();
    }

    private static void registerJava() {
        Set<String> keywords = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double",
            "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
        );
        languages.put("java", new Language("Java", keywords, "//", "/*", "*/"));
    }

    private static void registerPython() {
        Set<String> keywords = Set.of(
            "False", "None", "True", "and", "as", "assert", "async", "await",
            "break", "class", "continue", "def", "del", "elif", "else",
            "except", "finally", "for", "from", "global", "if", "import",
            "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise",
            "return", "try", "while", "with", "yield"
        );
        languages.put("python", new Language("Python", keywords, "#", "\"\"\"", "\"\"\""));
    }

    private static void registerJavaScript() {
        Set<String> keywords = Set.of(
            "abstract", "arguments", "await", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const", "continue", "debugger",
            "default", "delete", "do", "double", "else", "enum", "eval",
            "export", "extends", "false", "final", "finally", "float", "for",
            "function", "goto", "if", "implements", "import", "in",
            "instanceof", "int", "interface", "let", "long", "native", "new",
            "null", "package", "private", "protected", "public", "return",
            "short", "static", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true", "try", "typeof", "var",
            "void", "volatile", "while", "with", "yield"
        );
        languages.put("javascript", new Language("JavaScript", keywords, "//", "/*", "*/"));
    }

    private static void registerSQL() {
        Set<String> keywords = Set.of(
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE",
            "DROP", "ALTER", "TABLE", "DATABASE", "INDEX", "VIEW", "JOIN",
            "INNER", "LEFT", "RIGHT", "OUTER", "ON", "AND", "OR", "NOT",
            "NULL", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT",
            "UNIQUE", "DEFAULT", "AUTO_INCREMENT", "ORDER", "BY", "GROUP",
            "HAVING", "LIMIT", "OFFSET", "AS", "DISTINCT", "COUNT", "SUM",
            "AVG", "MIN", "MAX", "INT", "VARCHAR", "TEXT", "DATE", "DATETIME",
            "TIMESTAMP", "BOOLEAN"
        );
        languages.put("sql", new Language("SQL", keywords, "--", "/*", "*/"));
    }

    public static Language getLanguage(String name) {
        return languages.get(name.toLowerCase());
    }

    public static Set<String> getSupportedLanguages() {
        return languages.keySet();
    }

    public static void registerLanguage(String name, Language language) {
        languages.put(name.toLowerCase(), language);
    }
}
