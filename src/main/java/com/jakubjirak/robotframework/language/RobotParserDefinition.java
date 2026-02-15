package com.jakubjirak.robotframework.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class RobotParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(RobotLanguage.INSTANCE);
    public static final TokenSet COMMENTS = TokenSet.create(RobotTokenTypes.COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(RobotTokenTypes.STRING);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new RobotLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new RobotParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return new RobotPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new RobotFile(viewProvider);
    }
}
