/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.robotframework.language;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.jakubjirak.robotframework.analysis.RobotSections;
import com.jakubjirak.robotframework.analysis.RobotSyntax;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Hand-written lexer for Robot Framework data files, covering the syntax of
 * Robot Framework 7.4.
 *
 * <p>Section headers are recognized as whole lines, {@code *** Comments ***}
 * sections are lexed as comments, and control structure markers include the
 * {@code GROUP} block added in RF 7.2 as well as the multi-word markers
 * {@code ELSE IF}, {@code IN RANGE}, {@code IN ENUMERATE}, {@code IN ZIP} and
 * {@code WITH NAME}.</p>
 */
public class RobotLexer extends LexerBase {

    /** Lexing continues inside a {@code *** Comments ***} section. */
    private static final int STATE_COMMENTS_SECTION = 1;
    /** Lexing continues inside the values of a {@code [Tags]} setting. */
    private static final int STATE_TAGS = 2;

    private CharSequence buffer;
    private int bufferStart;
    private int bufferEnd;

    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    /** State that applies at {@link #getTokenStart()}, as required by the platform. */
    private int stateAtTokenStart;
    private int state;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferStart = startOffset;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.state = initialState;
        this.stateAtTokenStart = initialState;
        advance();
    }

    @Override
    public int getState() {
        return stateAtTokenStart;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        stateAtTokenStart = state;

        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }

        char c = buffer.charAt(tokenStart);

        if (Character.isWhitespace(c)) {
            tokenEnd = tokenStart;
            while (tokenEnd < bufferEnd && Character.isWhitespace(buffer.charAt(tokenEnd))) {
                tokenEnd++;
            }
            tokenType = RobotTokenTypes.WHITE_SPACE;
            return;
        }

        if (c == '*' && atLineStart(tokenStart)) {
            lexSectionHeader();
            return;
        }

        if (isInCommentsSection()) {
            // Everything inside *** Comments *** is ignored data.
            tokenEnd = lineEnd(tokenStart);
            tokenType = RobotTokenTypes.COMMENT;
            return;
        }

        if (c == '#') {
            tokenEnd = lineEnd(tokenStart);
            tokenType = RobotTokenTypes.COMMENT;
            return;
        }

        if (isVariableStart(tokenStart)) {
            lexVariable();
            return;
        }

        if (c == '"' || c == '\'') {
            lexString(c);
            return;
        }

        lexWord();
    }

    // ---- Section headers -------------------------------------------------------

    private void lexSectionHeader() {
        tokenEnd = lineEnd(tokenStart);
        String line = buffer.subSequence(tokenStart, tokenEnd).toString();
        String name = RobotSections.rawHeaderName(line);
        String canonical = name == null ? null : RobotSections.canonicalName(name);

        if (canonical == null) {
            setState(STATE_COMMENTS_SECTION, false);
            tokenType = RobotTokenTypes.SECTION_HEADER;
            return;
        }

        setState(STATE_COMMENTS_SECTION, "Comments".equals(canonical));
        setState(STATE_TAGS, false);

        switch (canonical) {
            case "Settings":
                tokenType = RobotTokenTypes.SETTINGS_HEADER;
                return;
            case "Variables":
                tokenType = RobotTokenTypes.VARIABLES_HEADER;
                return;
            case "Test Cases":
            case "Tasks":
                // Tasks are test cases under a different name; one token keeps every
                // consumer (folding, structure view, completion) working for both.
                tokenType = RobotTokenTypes.TEST_CASES_HEADER;
                return;
            case "Keywords":
                tokenType = RobotTokenTypes.KEYWORDS_HEADER;
                return;
            case "Comments":
                tokenType = RobotTokenTypes.COMMENTS_HEADER;
                return;
            default:
                tokenType = RobotTokenTypes.SECTION_HEADER;
        }
    }

    // ---- Variables, strings and words -----------------------------------------

    /**
     * Consumes a whole variable token including a Robot Framework 7.3 type
     * annotation, for example {@code ${amount: list[int]}}.
     */
    private void lexVariable() {
        int depth = 1;
        int i = tokenStart + 2;
        while (i < bufferEnd && depth > 0) {
            char ch = buffer.charAt(i);
            if (ch == '\n') {
                break;
            }
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
            }
            i++;
        }
        tokenEnd = i;
        tokenType = RobotTokenTypes.VARIABLE;
    }

    private void lexString(char quote) {
        int i = tokenStart + 1;
        while (i < bufferEnd) {
            char ch = buffer.charAt(i);
            if (ch == '\n') {
                break;
            }
            if (ch == quote) {
                i++;
                break;
            }
            if (ch == '\\') {
                i++;
            }
            i++;
        }
        tokenEnd = Math.min(i, bufferEnd);
        tokenType = RobotTokenTypes.STRING;
    }

    private void lexWord() {
        int i = tokenStart;
        while (i < bufferEnd && !Character.isWhitespace(buffer.charAt(i))) {
            i++;
        }
        String word = buffer.subSequence(tokenStart, i).toString();

        int extended = extendMultiWordMarker(word, i);
        if (extended > i) {
            i = extended;
            word = buffer.subSequence(tokenStart, i).toString();
        }
        tokenEnd = i;

        if (word.startsWith("[") && word.endsWith("]")) {
            setState(STATE_TAGS, word.equalsIgnoreCase("[Tags]"));
            tokenType = RobotTokenTypes.SETTING;
            return;
        }
        if (isInTagsContext() && !word.isEmpty()) {
            tokenType = RobotTokenTypes.TAG;
            return;
        }

        IElementType marker = markerTokenType(word);
        if (marker != null) {
            setState(STATE_TAGS, false);
            tokenType = marker;
            return;
        }
        if (isBuiltInKeyword(word)) {
            setState(STATE_TAGS, false);
            tokenType = RobotTokenTypes.KEYWORD;
            return;
        }
        tokenType = RobotTokenTypes.IDENTIFIER;
    }

    /**
     * Robot Framework separates cells by two spaces, so markers such as
     * {@code ELSE IF} and {@code IN RANGE} are a single cell containing a space.
     * Returns the offset after the extended marker, or {@code from} when the word
     * does not start a multi-word marker.
     */
    private int extendMultiWordMarker(String word, int from) {
        String upper = word.toUpperCase(Locale.ROOT);
        if (!"ELSE".equals(upper) && !"IN".equals(upper) && !"WITH".equals(upper)) {
            return from;
        }
        if (from >= bufferEnd || buffer.charAt(from) != ' ') {
            return from;
        }
        int next = from + 1;
        if (next >= bufferEnd || buffer.charAt(next) == ' ' || Character.isWhitespace(buffer.charAt(next))) {
            return from;
        }
        int end = next;
        while (end < bufferEnd && !Character.isWhitespace(buffer.charAt(end))) {
            end++;
        }
        String combined = word + " " + buffer.subSequence(next, end);
        String normalized = RobotSyntax.normalizeMarker(combined);
        if (RobotSyntax.isMarker(normalized) || "WITH NAME".equals(normalized)) {
            return end;
        }
        return from;
    }

    @Nullable
    private IElementType markerTokenType(String word) {
        if (!RobotSyntax.isMarker(word)) {
            return null;
        }
        switch (RobotSyntax.normalizeMarker(word)) {
            case "IF": return RobotTokenTypes.IF_KEYWORD;
            case "ELSE IF": return RobotTokenTypes.ELSE_IF_KEYWORD;
            case "ELSE": return RobotTokenTypes.ELSE_KEYWORD;
            case "TRY": return RobotTokenTypes.TRY_KEYWORD;
            case "EXCEPT": return RobotTokenTypes.EXCEPT_KEYWORD;
            case "FINALLY": return RobotTokenTypes.FINALLY_KEYWORD;
            case "WHILE": return RobotTokenTypes.WHILE_KEYWORD;
            case "FOR": return RobotTokenTypes.FOR_KEYWORD;
            case "GROUP": return RobotTokenTypes.GROUP_KEYWORD;
            case "END": return RobotTokenTypes.END_KEYWORD;
            case "BREAK": return RobotTokenTypes.BREAK_KEYWORD;
            case "CONTINUE": return RobotTokenTypes.CONTINUE_KEYWORD;
            case "RETURN": return RobotTokenTypes.RETURN_KEYWORD;
            case "VAR": return RobotTokenTypes.VAR_KEYWORD;
            case "IN": return RobotTokenTypes.IN_KEYWORD;
            case "IN RANGE": return RobotTokenTypes.IN_RANGE_KEYWORD;
            case "IN ENUMERATE": return RobotTokenTypes.IN_ENUMERATE_KEYWORD;
            case "IN ZIP": return RobotTokenTypes.IN_ZIP_KEYWORD;
            default: return RobotTokenTypes.KEYWORD;
        }
    }

    private boolean isBuiltInKeyword(String text) {
        // ponytail: a handful of very common BuiltIn keywords is enough for the
        // lexer; full keyword resolution belongs to completion and navigation.
        String lower = text.toLowerCase(Locale.ROOT).replace(" ", "");
        return lower.equals("log") || lower.equals("shouldbeequal")
                || lower.equals("shouldcontain") || lower.equals("run")
                || lower.equals("setvariable") || lower.equals("sleep");
    }

    // ---- Helpers ---------------------------------------------------------------

    private boolean isVariableStart(int offset) {
        char c = buffer.charAt(offset);
        return "$@&%".indexOf(c) >= 0
                && offset + 1 < bufferEnd
                && buffer.charAt(offset + 1) == '{';
    }

    private boolean atLineStart(int offset) {
        return offset == 0 || buffer.charAt(offset - 1) == '\n';
    }

    private int lineEnd(int offset) {
        int i = offset;
        while (i < bufferEnd && buffer.charAt(i) != '\n') {
            i++;
        }
        return i;
    }

    private boolean isInCommentsSection() {
        return (state & STATE_COMMENTS_SECTION) != 0;
    }

    private boolean isInTagsContext() {
        return (state & STATE_TAGS) != 0;
    }

    private void setState(int flag, boolean enabled) {
        if (enabled) {
            state |= flag;
        } else {
            state &= ~flag;
        }
    }
}
