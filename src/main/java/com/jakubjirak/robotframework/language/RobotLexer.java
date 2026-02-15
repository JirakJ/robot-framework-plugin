package com.jakubjirak.robotframework.language;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotLexer extends LexerBase {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType tokenType;
    private boolean inTagsContext = false;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        this.inTagsContext = false;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return startOffset;
    }

    @Override
    public int getTokenEnd() {
        return currentOffset;
    }

    @Override
    public void advance() {
        if (currentOffset >= endOffset) {
            tokenType = null;
            return;
        }

        char c = buffer.charAt(currentOffset);
        
        // Skip whitespace
        if (Character.isWhitespace(c)) {
            while (currentOffset < endOffset && Character.isWhitespace(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            tokenType = RobotTokenTypes.WHITE_SPACE;
            return;
        }

        // Comments
        if (c == '#') {
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '\n') {
                currentOffset++;
            }
            tokenType = RobotTokenTypes.COMMENT;
            return;
        }

        // Variables
        if (c == '$' || c == '@' || c == '&' || c == '%') {
            if (currentOffset + 1 < endOffset && buffer.charAt(currentOffset + 1) == '{') {
                int braceDepth = 1;
                currentOffset += 2;
                while (currentOffset < endOffset && braceDepth > 0) {
                    char ch = buffer.charAt(currentOffset);
                    if (ch == '{') braceDepth++;
                    if (ch == '}') braceDepth--;
                    currentOffset++;
                }
                tokenType = RobotTokenTypes.VARIABLE;
                return;
            }
        }

        // Section headers
        if (c == '*') {
            int start = currentOffset;
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '*') {
                currentOffset++;
            }
            if (currentOffset < endOffset) currentOffset++; // Include closing *
            String text = buffer.subSequence(start, currentOffset).toString().toLowerCase();
            
            if (text.contains("settings") || text.contains("setting")) {
                tokenType = RobotTokenTypes.SETTINGS_HEADER;
            } else if (text.contains("variables") || text.contains("variable")) {
                tokenType = RobotTokenTypes.VARIABLES_HEADER;
            } else if (text.contains("test cases") || text.contains("test case")) {
                tokenType = RobotTokenTypes.TEST_CASES_HEADER;
            } else if (text.contains("keywords") || text.contains("keyword")) {
                tokenType = RobotTokenTypes.KEYWORDS_HEADER;
            } else {
                tokenType = RobotTokenTypes.SECTION_HEADER;
            }
            return;
        }

        // String literals
        if (c == '"' || c == '\'') {
            char quote = c;
            currentOffset++;
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (ch == quote) {
                    currentOffset++;
                    break;
                }
                if (ch == '\\') currentOffset++; // Skip escaped char
                currentOffset++;
            }
            tokenType = RobotTokenTypes.STRING;
            return;
        }

        // Keywords and identifiers
        int start = currentOffset;
        while (currentOffset < endOffset && !Character.isWhitespace(buffer.charAt(currentOffset))) {
            currentOffset++;
        }
        
        String text = buffer.subSequence(start, currentOffset).toString();
        
        // Check if it's a setting keyword
        if (text.startsWith("[") && text.endsWith("]")) {
            // Check if this is [Tags] setting
            if (text.equalsIgnoreCase("[Tags]")) {
                inTagsContext = true;
            } else {
                inTagsContext = false;
            }
            tokenType = RobotTokenTypes.SETTING;
        } else if (inTagsContext && !text.isEmpty()) {
            // If we're after [Tags], treat tokens as tags
            tokenType = RobotTokenTypes.TAG;
        } else if (isControlFlowKeyword(text)) {
            inTagsContext = false;
            tokenType = getControlFlowTokenType(text);
        } else if (isKeyword(text)) {
            inTagsContext = false;
            tokenType = RobotTokenTypes.KEYWORD;
        } else {
            tokenType = RobotTokenTypes.IDENTIFIER;
        }
    }

    private boolean isControlFlowKeyword(String text) {
        String upper = text.toUpperCase().trim();
        return upper.equals("IF") || upper.equals("ELSE IF") || upper.equals("ELSE") ||
               upper.equals("TRY") || upper.equals("EXCEPT") || upper.equals("FINALLY") ||
               upper.equals("WHILE") || upper.equals("FOR") || upper.equals("END") ||
               upper.equals("BREAK") || upper.equals("CONTINUE") || upper.equals("RETURN") ||
               upper.equals("VAR") || upper.equals("IN") || upper.equals("IN RANGE") ||
               upper.equals("IN ENUMERATE") || upper.equals("IN ZIP");
    }

    private IElementType getControlFlowTokenType(String text) {
        String upper = text.toUpperCase().trim();
        switch (upper) {
            case "IF": return RobotTokenTypes.IF_KEYWORD;
            case "ELSE IF": return RobotTokenTypes.ELSE_IF_KEYWORD;
            case "ELSE": return RobotTokenTypes.ELSE_KEYWORD;
            case "TRY": return RobotTokenTypes.TRY_KEYWORD;
            case "EXCEPT": return RobotTokenTypes.EXCEPT_KEYWORD;
            case "FINALLY": return RobotTokenTypes.FINALLY_KEYWORD;
            case "WHILE": return RobotTokenTypes.WHILE_KEYWORD;
            case "FOR": return RobotTokenTypes.FOR_KEYWORD;
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

    private boolean isKeyword(String text) {
        // Common Robot Framework built-in keywords
        String lower = text.toLowerCase().replace(" ", "");
        return lower.equals("log") || lower.equals("shouldbeequal") || 
               lower.equals("shouldcontain") || lower.equals("run") ||
               lower.equals("setvariable") || lower.equals("sleep");
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }
}
