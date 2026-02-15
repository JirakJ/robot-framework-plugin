package com.jakubjirak.robotframework.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RobotFoldingBuilder extends FoldingBuilderEx {
    
    @NotNull
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root,
                                                          @NotNull Document document,
                                                          boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        
        // Find all foldable regions
        collectFoldingRegions(root, descriptors, document);
        
        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }
    
    private void collectFoldingRegions(PsiElement element, List<FoldingDescriptor> descriptors, Document document) {
        ASTNode node = element.getNode();
        if (node == null) return;
        
        // Fold sections (Settings, Variables, Test Cases, Keywords)
        if (node.getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
            node.getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
            node.getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
            node.getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
            
            TextRange range = getSectionRange(element, document);
            if (range != null && range.getLength() > 0) {
                descriptors.add(new FoldingDescriptor(node, range));
            }
        }
        
        // Fold control flow blocks (IF, TRY, WHILE, FOR)
        if (node.getElementType() == RobotTokenTypes.IF_KEYWORD ||
            node.getElementType() == RobotTokenTypes.TRY_KEYWORD ||
            node.getElementType() == RobotTokenTypes.WHILE_KEYWORD ||
            node.getElementType() == RobotTokenTypes.FOR_KEYWORD) {
            
            TextRange range = getControlFlowBlockRange(element, document);
            if (range != null && range.getLength() > 0) {
                descriptors.add(new FoldingDescriptor(node, range));
            }
        }
        
        // Recursively process children
        for (PsiElement child : element.getChildren()) {
            collectFoldingRegions(child, descriptors, document);
        }
    }
    
    private TextRange getSectionRange(PsiElement sectionHeader, Document document) {
        int startOffset = sectionHeader.getTextRange().getEndOffset();
        int endOffset = document.getTextLength();
        
        // Find the next section header or end of file
        PsiElement sibling = sectionHeader.getNextSibling();
        while (sibling != null) {
            ASTNode siblingNode = sibling.getNode();
            if (siblingNode != null && (
                siblingNode.getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                siblingNode.getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                siblingNode.getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                siblingNode.getElementType() == RobotTokenTypes.KEYWORDS_HEADER)) {
                endOffset = sibling.getTextRange().getStartOffset();
                break;
            }
            sibling = sibling.getNextSibling();
        }
        
        return new TextRange(startOffset, endOffset);
    }
    
    private TextRange getControlFlowBlockRange(PsiElement controlFlowKeyword, Document document) {
        int startOffset = controlFlowKeyword.getTextRange().getEndOffset();
        int endOffset = startOffset;
        
        // Find matching END keyword
        PsiElement sibling = controlFlowKeyword.getNextSibling();
        int depth = 1;
        
        while (sibling != null && depth > 0) {
            ASTNode siblingNode = sibling.getNode();
            if (siblingNode != null) {
                // Increment depth for nested control flow
                if (siblingNode.getElementType() == RobotTokenTypes.IF_KEYWORD ||
                    siblingNode.getElementType() == RobotTokenTypes.TRY_KEYWORD ||
                    siblingNode.getElementType() == RobotTokenTypes.WHILE_KEYWORD ||
                    siblingNode.getElementType() == RobotTokenTypes.FOR_KEYWORD) {
                    depth++;
                }
                // Decrement depth for END
                if (siblingNode.getElementType() == RobotTokenTypes.END_KEYWORD) {
                    depth--;
                    if (depth == 0) {
                        endOffset = sibling.getTextRange().getStartOffset();
                        break;
                    }
                }
            }
            sibling = sibling.getNextSibling();
        }
        
        if (endOffset > startOffset) {
            return new TextRange(startOffset, endOffset);
        }
        return null;
    }
    
    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == RobotTokenTypes.SETTINGS_HEADER) return "*** Settings ***...";
        if (node.getElementType() == RobotTokenTypes.VARIABLES_HEADER) return "*** Variables ***...";
        if (node.getElementType() == RobotTokenTypes.TEST_CASES_HEADER) return "*** Test Cases ***...";
        if (node.getElementType() == RobotTokenTypes.KEYWORDS_HEADER) return "*** Keywords ***...";
        if (node.getElementType() == RobotTokenTypes.IF_KEYWORD) return "IF...END";
        if (node.getElementType() == RobotTokenTypes.TRY_KEYWORD) return "TRY...END";
        if (node.getElementType() == RobotTokenTypes.WHILE_KEYWORD) return "WHILE...END";
        if (node.getElementType() == RobotTokenTypes.FOR_KEYWORD) return "FOR...END";
        return "...";
    }
    
    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
