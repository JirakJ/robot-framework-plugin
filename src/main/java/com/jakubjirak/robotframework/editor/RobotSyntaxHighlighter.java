package com.jakubjirak.robotframework.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.jakubjirak.robotframework.language.RobotLexer;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class RobotSyntaxHighlighter extends SyntaxHighlighterBase {
    
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("ROBOT_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ROBOT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    
    public static final TextAttributesKey CONTROL_FLOW =
            createTextAttributesKey("ROBOT_CONTROL_FLOW", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("ROBOT_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    
    public static final TextAttributesKey STRING =
            createTextAttributesKey("ROBOT_STRING", DefaultLanguageHighlighterColors.STRING);
    
    public static final TextAttributesKey SECTION_HEADER =
            createTextAttributesKey("ROBOT_SECTION", DefaultLanguageHighlighterColors.METADATA);
    
    public static final TextAttributesKey SETTING =
            createTextAttributesKey("ROBOT_SETTING", DefaultLanguageHighlighterColors.METADATA);
    
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("ROBOT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    public static final TextAttributesKey EMBEDDED_ARGUMENT =
            createTextAttributesKey("ROBOT_EMBEDDED_ARG", DefaultLanguageHighlighterColors.PARAMETER);

    public static final TextAttributesKey TAG =
            createTextAttributesKey("ROBOT_TAG", DefaultLanguageHighlighterColors.MARKUP_TAG);

    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] CONTROL_FLOW_KEYS = new TextAttributesKey[]{CONTROL_FLOW};
    private static final TextAttributesKey[] VARIABLE_KEYS = new TextAttributesKey[]{VARIABLE};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] SECTION_KEYS = new TextAttributesKey[]{SECTION_HEADER};
    private static final TextAttributesKey[] SETTING_KEYS = new TextAttributesKey[]{SETTING};
    private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
    private static final TextAttributesKey[] EMBEDDED_ARG_KEYS = new TextAttributesKey[]{EMBEDDED_ARGUMENT};
    private static final TextAttributesKey[] TAG_KEYS = new TextAttributesKey[]{TAG};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new RobotLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(RobotTokenTypes.COMMENT)) {
            return COMMENT_KEYS;
        }
        // RF 7+ Control Flow Keywords
        if (tokenType.equals(RobotTokenTypes.IF_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.ELSE_IF_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.ELSE_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.TRY_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.EXCEPT_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.FINALLY_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.WHILE_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.FOR_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.END_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.BREAK_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.CONTINUE_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.RETURN_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.VAR_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.IN_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.IN_RANGE_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.IN_ENUMERATE_KEYWORD) ||
            tokenType.equals(RobotTokenTypes.IN_ZIP_KEYWORD)) {
            return CONTROL_FLOW_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.KEYWORD)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.VARIABLE)) {
            return VARIABLE_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.STRING)) {
            return STRING_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.SECTION_HEADER) ||
            tokenType.equals(RobotTokenTypes.SETTINGS_HEADER) ||
            tokenType.equals(RobotTokenTypes.VARIABLES_HEADER) ||
            tokenType.equals(RobotTokenTypes.TEST_CASES_HEADER) ||
            tokenType.equals(RobotTokenTypes.KEYWORDS_HEADER)) {
            return SECTION_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.SETTING)) {
            return SETTING_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.EMBEDDED_ARGUMENT)) {
            return EMBEDDED_ARG_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.TAG)) {
            return TAG_KEYS;
        }
        if (tokenType.equals(RobotTokenTypes.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }
        return EMPTY_KEYS;
    }
}
