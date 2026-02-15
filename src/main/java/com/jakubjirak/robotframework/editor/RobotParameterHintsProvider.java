package com.jakubjirak.robotframework.editor;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides parameter name hints for Robot Framework keyword calls.
 * Shows parameter names inline for better code readability.
 */
@SuppressWarnings("UnstableApiUsage")
public class RobotParameterHintsProvider implements InlayHintsProvider<NoSettings> {

    private static final SettingsKey<NoSettings> KEY = new SettingsKey<>("robot.parameter.hints");

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile file,
                                                @NotNull Editor editor,
                                                @NotNull NoSettings settings,
                                                @NotNull InlayHintsSink sink) {
        return new ParameterHintsCollector(editor);
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @NotNull
    @Override
    public String getName() {
        return "Robot Framework parameter hints";
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "*** Test Cases ***\n" +
               "Example Test\n" +
               "    Log    message    level=INFO\n" +
               "    Should Be Equal    first    second    msg=Not equal";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings settings) {
        return new ImmediateConfigurable() {
            @NotNull
            @Override
            public JComponent createComponent(@NotNull ChangeListener listener) {
                return new JPanel();
            }

            @NotNull
            public String getMainCheckboxText() {
                return "Show parameter name hints";
            }

            @NotNull
            public String getCaseDescription(@NotNull ImmediateConfigurable.Case aCase) {
                return "";
            }
        };
    }

    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    private static class ParameterHintsCollector extends FactoryInlayHintsCollector {

        public ParameterHintsCollector(@NotNull Editor editor) {
            super(editor);
        }

        @Override
        public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink sink) {
            // Check if this is a keyword call with arguments
            if (element.getNode() != null &&
                element.getNode().getElementType() == RobotTokenTypes.KEYWORD) {

                // Get next siblings to find arguments
                PsiElement sibling = element.getNextSibling();
                int argIndex = 0;

                while (sibling != null) {
                    if (sibling.getNode() != null &&
                        sibling.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {

                        String argText = sibling.getText().trim();

                        // Don't show hint if argument already has name (contains =)
                        if (!argText.contains("=") && !argText.isEmpty()) {
                            // Create hint for parameter name
                            String hintText = "arg" + argIndex + ":";

                            PresentationFactory factory = getFactory();
                            InlayPresentation presentation = factory.smallText(hintText);

                            sink.addInlineElement(
                                sibling.getTextRange().getStartOffset(),
                                false,
                                presentation,
                                false
                            );

                            argIndex++;
                        }
                    }

                    sibling = sibling.getNextSibling();
                }
            }

            return true;
        }
    }
}

