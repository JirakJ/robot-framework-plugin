package com.jakubjirak.robotframework.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.jakubjirak.robotframework.language.RobotTokenTypes;

/**
 * Quote handler for Robot Framework strings.
 * Enables automatic quote matching and smart quote handling in the editor.
 */
public class RobotQuoteHandler extends SimpleTokenSetQuoteHandler {
    
    public RobotQuoteHandler() {
        super(RobotTokenTypes.STRING);
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        // Consider both " and ' as opening quotes
        char c = iterator.getDocument().getCharsSequence().charAt(offset);
        return c == '"' || c == '\'';
    }

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        // Consider both " and ' as closing quotes
        char c = iterator.getDocument().getCharsSequence().charAt(offset);
        return c == '"' || c == '\'';
    }
}

