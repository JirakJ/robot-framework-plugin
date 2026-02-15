package com.jakubjirak.robotframework.language;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

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
    IElementType TEST_CASES_HEADER = new RobotElementType("TEST_CASES_HEADER");
    IElementType KEYWORDS_HEADER = new RobotElementType("KEYWORDS_HEADER");
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

    // FOR loop modifiers
    IElementType IN_KEYWORD = new RobotElementType("IN_KEYWORD");
    IElementType IN_RANGE_KEYWORD = new RobotElementType("IN_RANGE_KEYWORD");
    IElementType IN_ENUMERATE_KEYWORD = new RobotElementType("IN_ENUMERATE_KEYWORD");
    IElementType IN_ZIP_KEYWORD = new RobotElementType("IN_ZIP_KEYWORD");

    // Embedded arguments
    IElementType EMBEDDED_ARGUMENT = new RobotElementType("EMBEDDED_ARGUMENT");

    // Tags
    IElementType TAG = new RobotElementType("TAG");
}
