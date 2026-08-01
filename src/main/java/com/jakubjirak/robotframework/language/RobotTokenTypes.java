package com.jakubjirak.robotframework.language;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface RobotTokenTypes {
    IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    IElementType COMMENT = new RobotElementType("COMMENT");
    IElementType IDENTIFIER = new RobotElementType("IDENTIFIER");
    IElementType KEYWORD = new RobotElementType("KEYWORD");
    IElementType VARIABLE = new RobotElementType("VARIABLE");
    IElementType STRING = new RobotElementType("STRING");
    IElementType SECTION_HEADER = new RobotElementType("SECTION_HEADER");
    IElementType SETTINGS_HEADER = new RobotElementType("SETTINGS_HEADER");
    IElementType VARIABLES_HEADER = new RobotElementType("VARIABLES_HEADER");
    /** Also used for {@code *** Tasks ***}, which is the same section under another name. */
    IElementType TEST_CASES_HEADER = new RobotElementType("TEST_CASES_HEADER");
    IElementType KEYWORDS_HEADER = new RobotElementType("KEYWORDS_HEADER");
    IElementType COMMENTS_HEADER = new RobotElementType("COMMENTS_HEADER");
    IElementType SETTING = new RobotElementType("SETTING");
    IElementType NUMBER = new RobotElementType("NUMBER");
    IElementType OPERATOR = new RobotElementType("OPERATOR");

    // RF 7+ Control Flow Keywords
    IElementType IF_KEYWORD = new RobotElementType("IF_KEYWORD");
    IElementType ELSE_IF_KEYWORD = new RobotElementType("ELSE_IF_KEYWORD");
    IElementType ELSE_KEYWORD = new RobotElementType("ELSE_KEYWORD");
    IElementType TRY_KEYWORD = new RobotElementType("TRY_KEYWORD");
    IElementType EXCEPT_KEYWORD = new RobotElementType("EXCEPT_KEYWORD");
    IElementType FINALLY_KEYWORD = new RobotElementType("FINALLY_KEYWORD");
    IElementType WHILE_KEYWORD = new RobotElementType("WHILE_KEYWORD");
    IElementType FOR_KEYWORD = new RobotElementType("FOR_KEYWORD");
    IElementType END_KEYWORD = new RobotElementType("END");
    IElementType BREAK_KEYWORD = new RobotElementType("BREAK_KEYWORD");
    IElementType CONTINUE_KEYWORD = new RobotElementType("CONTINUE_KEYWORD");
    IElementType RETURN_KEYWORD = new RobotElementType("RETURN_KEYWORD");
    IElementType VAR_KEYWORD = new RobotElementType("VAR_KEYWORD");
    /** {@code GROUP} block, added in Robot Framework 7.2. */
    IElementType GROUP_KEYWORD = new RobotElementType("GROUP_KEYWORD");

    // FOR loop modifiers
    IElementType IN_KEYWORD = new RobotElementType("IN_KEYWORD");
    IElementType IN_RANGE_KEYWORD = new RobotElementType("IN_RANGE_KEYWORD");
    IElementType IN_ENUMERATE_KEYWORD = new RobotElementType("IN_ENUMERATE_KEYWORD");
    IElementType IN_ZIP_KEYWORD = new RobotElementType("IN_ZIP_KEYWORD");

    // Embedded arguments
    IElementType EMBEDDED_ARGUMENT = new RobotElementType("EMBEDDED_ARGUMENT");

    // Tags
    IElementType TAG = new RobotElementType("TAG");

    /** Every {@code *** ... ***} header token. */
    TokenSet SECTION_HEADERS = TokenSet.create(
            SECTION_HEADER, SETTINGS_HEADER, VARIABLES_HEADER,
            TEST_CASES_HEADER, KEYWORDS_HEADER, COMMENTS_HEADER);

    /** Markers that open a block terminated by {@code END}. {@code GROUP} is RF 7.2. */
    TokenSet BLOCK_OPENERS = TokenSet.create(
            IF_KEYWORD, FOR_KEYWORD, WHILE_KEYWORD, TRY_KEYWORD, GROUP_KEYWORD);

    /** All Robot Framework control structure markers. */
    TokenSet CONTROL_FLOW = TokenSet.create(
            IF_KEYWORD, ELSE_IF_KEYWORD, ELSE_KEYWORD, TRY_KEYWORD, EXCEPT_KEYWORD,
            FINALLY_KEYWORD, WHILE_KEYWORD, FOR_KEYWORD, GROUP_KEYWORD, END_KEYWORD,
            BREAK_KEYWORD, CONTINUE_KEYWORD, RETURN_KEYWORD, VAR_KEYWORD,
            IN_KEYWORD, IN_RANGE_KEYWORD, IN_ENUMERATE_KEYWORD, IN_ZIP_KEYWORD);
}
