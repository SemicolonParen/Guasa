package com.gdkteam.guasa_synload;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private final Language language;

    public SyntaxHighlighter(Language language) {
        this.language = language;
    }

    public List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();
        String[] lines = code.split("\n", -1);

        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            int column = 0;

            while (column < line.length()) {
                Token token = extractNextToken(line, column, lineNum + 1);
                if (token != null) {
                    tokens.add(token);
                    column += token.getValue().length();
                } else {
                    column++;
                }
            }
        }

        return tokens;
    }

    private Token extractNextToken(String line, int startCol, int lineNum) {
        if (startCol >= line.length()) {
            return null;
        }

        String remaining = line.substring(startCol);

        if (remaining.startsWith(language.getSingleLineComment())) {
            return new Token(TokenType.COMMENT, remaining, lineNum, startCol);
        }

        if (Character.isWhitespace(remaining.charAt(0))) {
            int end = 1;
            while (end < remaining.length() && Character.isWhitespace(remaining.charAt(end))) {
                end++;
            }
            return new Token(TokenType.WHITESPACE, remaining.substring(0, end), lineNum, startCol);
        }

        if (remaining.charAt(0) == '"' || remaining.charAt(0) == '\'') {
            return extractStringLiteral(remaining, lineNum, startCol);
        }

        if (Character.isDigit(remaining.charAt(0))) {
            return extractNumber(remaining, lineNum, startCol);
        }

        if (Character.isLetter(remaining.charAt(0)) || remaining.charAt(0) == '_') {
            return extractIdentifierOrKeyword(remaining, lineNum, startCol);
        }

        if (isOperator(remaining.charAt(0))) {
            return extractOperator(remaining, lineNum, startCol);
        }

        if (isBracket(remaining.charAt(0))) {
            return new Token(TokenType.BRACKET, String.valueOf(remaining.charAt(0)), lineNum, startCol);
        }

        return new Token(TokenType.UNKNOWN, String.valueOf(remaining.charAt(0)), lineNum, startCol);
    }

    private Token extractStringLiteral(String text, int line, int col) {
        char quote = text.charAt(0);
        int end = 1;
        while (end < text.length()) {
            if (text.charAt(end) == quote && text.charAt(end - 1) != '\\') {
                end++;
                break;
            }
            end++;
        }
        return new Token(TokenType.STRING_LITERAL, text.substring(0, end), line, col);
    }

    private Token extractNumber(String text, int line, int col) {
        int end = 0;
        while (end < text.length() && (Character.isDigit(text.charAt(end)) || text.charAt(end) == '.')) {
            end++;
        }
        return new Token(TokenType.NUMBER_LITERAL, text.substring(0, end), line, col);
    }

    private Token extractIdentifierOrKeyword(String text, int line, int col) {
        int end = 0;
        while (end < text.length() &&
               (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '_')) {
            end++;
        }
        String value = text.substring(0, end);
        TokenType type = language.getKeywords().contains(value) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
        return new Token(type, value, line, col);
    }

    private Token extractOperator(String text, int line, int col) {
        int end = 1;
        while (end < text.length() && isOperator(text.charAt(end))) {
            end++;
        }
        return new Token(TokenType.OPERATOR, text.substring(0, end), line, col);
    }

    private boolean isOperator(char c) {
        return "+-*/%=<>!&|^~".indexOf(c) != -1;
    }

    private boolean isBracket(char c) {
        return "()[]{}".indexOf(c) != -1;
    }

    public String highlight(String code) {
        List<Token> tokens = tokenize(code);
        StringBuilder highlighted = new StringBuilder();

        for (Token token : tokens) {
            String colored = colorize(token);
            highlighted.append(colored);
        }

        return highlighted.toString();
    }

    private String colorize(Token token) {
        String ansiCode = getAnsiColor(token.getType());
        return ansiCode + token.getValue() + "\u001B[0m";
    }

    private String getAnsiColor(TokenType type) {
        switch (type) {
            case KEYWORD:
                return "\u001B[35m";
            case STRING_LITERAL:
                return "\u001B[32m";
            case NUMBER_LITERAL:
                return "\u001B[36m";
            case COMMENT:
                return "\u001B[90m";
            case OPERATOR:
                return "\u001B[33m";
            case FUNCTION:
                return "\u001B[34m";
            default:
                return "\u001B[0m";
        }
    }
}
