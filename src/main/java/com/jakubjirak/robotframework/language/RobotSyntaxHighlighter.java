package com.jakubjirak.robotframework.language;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Syntax highlighter for Robot Framework language.
 */
public class RobotSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey SECTION_HEADER =
            createTextAttributesKey("ROBOT_SECTION_HEADER", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("ROBOT_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ROBOT_KEYWORD", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    public static final TextAttributesKey SETTING =
            createTextAttributesKey("ROBOT_SETTING", DefaultLanguageHighlighterColors.METADATA);

    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("ROBOT_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);

    public static final TextAttributesKey STRING =
            createTextAttributesKey("ROBOT_STRING", DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("ROBOT_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("ROBOT_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PARAMETER =
            createTextAttributesKey("ROBOT_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public com.intellij.lexer.Lexer getHighlightingLexer() {
        return new RobotLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType == RobotTokenTypes.COMMENT) {
            return pack(COMMENT);
        } else if (tokenType == RobotTokenTypes.SETTINGS_HEADER ||
                   tokenType == RobotTokenTypes.VARIABLES_HEADER ||
                   tokenType == RobotTokenTypes.TEST_CASES_HEADER ||
                   tokenType == RobotTokenTypes.KEYWORDS_HEADER) {
            return pack(SECTION_HEADER);
        } else if (tokenType == RobotTokenTypes.KEYWORD) {
            return pack(KEYWORD);
        } else if (tokenType == RobotTokenTypes.SETTING) {
            return pack(SETTING);
        } else if (tokenType == RobotTokenTypes.VARIABLE) {
            return pack(VARIABLE);
        } else if (tokenType == RobotTokenTypes.STRING) {
            return pack(STRING);
        } else if (tokenType == RobotTokenTypes.NUMBER) {
            return pack(NUMBER);
        }
        return EMPTY_KEYS;
    }
}

