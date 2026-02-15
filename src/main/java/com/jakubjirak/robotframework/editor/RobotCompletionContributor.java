package com.jakubjirak.robotframework.editor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jakubjirak.robotframework.language.RobotLanguage;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

public class RobotCompletionContributor extends CompletionContributor {
    
    public RobotCompletionContributor() {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(RobotLanguage.INSTANCE),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                            @NotNull ProcessingContext context,
                                            @NotNull CompletionResultSet result) {
                    
                    // RF 7+ Control Flow Keywords
                    addControlFlowKeywords(result);

                    // Built-in keywords
                    addBuiltInKeywords(result);
                    
                    // Variables
                    addCommonVariables(result);
                    
                    // Settings
                    addSettings(result);

                    // Settings section keywords (Library, Resource, etc.)
                    addSettingsSectionKeywords(result);

                    // Variables from current file
                    addVariablesFromFile(parameters, result);

                    // Keywords from current file
                    addKeywordsFromFile(parameters, result);

                    // Section headers
                    addSectionHeaders(result);
                }
            });
    }

    private void addControlFlowKeywords(CompletionResultSet result) {
        String[] keywords = {
            "IF", "ELSE IF", "ELSE", "END",
            "TRY", "EXCEPT", "FINALLY",
            "WHILE", "FOR", "BREAK", "CONTINUE",
            "RETURN", "VAR"
        };

        for (String keyword : keywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                .withTypeText("RF 7+ Control Flow")
                .withBoldness(true)
                .withTailText(" (Robot Framework 7+)", true));
        }
    }

    private void addBuiltInKeywords(CompletionResultSet result) {
        // BuiltIn library keywords
        String[] builtInKeywords = {
            "Log", "Log To Console", "Log Many", "Log Variables",
            "Should Be Equal", "Should Not Be Equal", "Should Contain", "Should Not Contain",
            "Should Be True", "Should Be False", "Should Be Empty", "Should Not Be Empty",
            "Should Match", "Should Not Match", "Should Match Regexp", "Should Not Match Regexp",
            "Should Start With", "Should Not Start With", "Should End With", "Should Not End With",
            "Set Variable", "Set Variable If", "Set Test Variable", "Set Suite Variable", "Set Global Variable",
            "Create List", "Create Dictionary", "Append To List", "Remove From List", "Get From List",
            "Set To Dictionary", "Get From Dictionary", "Remove From Dictionary", "Get Dictionary Keys", "Get Dictionary Values",
            "Run Keyword", "Run Keyword If", "Run Keyword Unless", "Run Keywords",
            "Run Keyword And Continue On Failure", "Run Keyword And Expect Error", "Run Keyword And Ignore Error",
            "Wait Until Keyword Succeeds", "Repeat Keyword", "Run Keyword And Return", "Run Keyword And Return If",
            "Pass Execution", "Fail", "Fatal Error", "Skip", "Skip If",
            "Get Length", "Get Count", "Length Should Be", "Should Be Equal As Integers", "Should Be Equal As Numbers", "Should Be Equal As Strings",
            "Convert To Integer", "Convert To Number", "Convert To String", "Convert To Boolean",
            "Sleep", "Catenate", "Get Time", "Evaluate", "Import Library", "Import Resource", "Import Variables",
            "Set Tags", "Remove Tags", "Get Library Instance", "Reload Library",
            "No Operation", "Comment", "Continue For Loop", "Continue For Loop If", "Exit For Loop", "Exit For Loop If",
            "Return From Keyword", "Return From Keyword If"
        };

        // Collections library keywords
        String[] collectionsKeywords = {
            "Append To List", "Combine Lists", "Copy List", "Count Values In List", "Get From List",
            "Get Index From List", "Get Match Count", "Get Matches", "Get Slice From List",
            "Insert Into List", "Keep In Dictionary", "List Should Contain Sub List", "List Should Contain Value",
            "List Should Not Contain Duplicates", "List Should Not Contain Value", "Lists Should Be Equal",
            "Pop From Dictionary", "Remove Duplicates", "Remove From List", "Remove Values From List",
            "Reverse List", "Set List Value", "Set To Dictionary", "Should Contain Match", "Should Not Contain Match",
            "Sort List"
        };

        // String library keywords
        String[] stringKeywords = {
            "Convert To Lowercase", "Convert To Uppercase", "Convert To Title Case",
            "Encode String To Bytes", "Decode Bytes To String", "Format String",
            "Get Line", "Get Line Count", "Get Lines Containing String", "Get Lines Matching Pattern", "Get Lines Matching Regexp",
            "Get Regexp Matches", "Get Substring", "Remove String", "Remove String Using Regexp",
            "Replace String", "Replace String Using Regexp", "Should Be String", "Should Not Be String",
            "Split String", "Split String From Right", "Split String To Characters", "Split To Lines",
            "Strip String", "Fetch From Left", "Fetch From Right"
        };
        
        for (String keyword : builtInKeywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                .withTypeText("BuiltIn Library")
                .withTailText(" (BuiltIn)", true));
        }

        for (String keyword : collectionsKeywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                .withTypeText("Collections Library")
                .withTailText(" (Collections)", true));
        }

        for (String keyword : stringKeywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                .withTypeText("String Library")
                .withTailText(" (String)", true));
        }
    }
    
    private void addCommonVariables(CompletionResultSet result) {
        String[] variables = {
            "${CURDIR}", "${TEMPDIR}", "${EXECDIR}", "${/}", "${:}",
            "${SPACE}", "${EMPTY}", "${True}", "${False}", "${None}",
            "${TEST NAME}", "${TEST DOCUMENTATION}", "${TEST STATUS}",
            "${TEST MESSAGE}", "${SUITE NAME}", "${SUITE SOURCE}",
            "${SUITE DOCUMENTATION}", "${KEYWORD STATUS}", "${KEYWORD MESSAGE}",
            "${LOG LEVEL}", "${OUTPUT DIR}", "${OUTPUT FILE}"
        };
        
        for (String variable : variables) {
            result.addElement(LookupElementBuilder.create(variable)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Variable)
                .withTypeText("Built-in Variable"));
        }
    }
    
    private void addSettings(CompletionResultSet result) {
        String[] settings = {
            "[Documentation]", "[Tags]", "[Setup]", "[Teardown]",
            "[Template]", "[Timeout]", "[Arguments]", "[Return]"
        };
        
        for (String setting : settings) {
            result.addElement(LookupElementBuilder.create(setting)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Annotationtype)
                .withTypeText("Setting"));
        }
    }

    private void addSettingsSectionKeywords(CompletionResultSet result) {
        String[] settingsKeywords = {
            "Library", "Resource", "Variables", "Documentation",
            "Metadata", "Suite Setup", "Suite Teardown", "Test Setup", "Test Teardown",
            "Test Template", "Test Timeout", "Force Tags", "Default Tags",
            "Task Setup", "Task Teardown", "Task Template", "Task Timeout"
        };

        for (String keyword : settingsKeywords) {
            result.addElement(LookupElementBuilder.create(keyword)
                .withIcon(com.intellij.icons.AllIcons.General.Settings)
                .withTypeText("Settings Section")
                .withTailText(" (*** Settings ***)", true));
        }
    }

    private void addVariablesFromFile(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile file = parameters.getOriginalFile();
        if (file == null) return;

        // Look for variables in *** Variables *** section
        boolean inVariablesSection = false;

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            // Check if we entered Variables section
            if (child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
                inVariablesSection = true;
                continue;
            }

            // Check if we left Variables section
            if (inVariablesSection && (
                child.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER)) {
                break;
            }

            // Extract variable names
            if (inVariablesSection && child.getNode().getElementType() == RobotTokenTypes.VARIABLE) {
                String varText = child.getText().trim();
                if (!varText.isEmpty()) {
                    result.addElement(LookupElementBuilder.create(varText)
                        .withIcon(com.intellij.icons.AllIcons.Nodes.Variable)
                        .withTypeText("File Variable")
                        .withTailText(" (defined in this file)", true));
                }
            }
        }
    }

    private void addKeywordsFromFile(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile file = parameters.getOriginalFile();
        if (file == null) return;

        // Look for keywords in *** Keywords *** section
        boolean inKeywordsSection = false;

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            // Check if we entered Keywords section
            if (child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                inKeywordsSection = true;
                continue;
            }

            // Check if we left Keywords section
            if (inKeywordsSection && (
                child.getNode().getElementType() == RobotTokenTypes.SETTINGS_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER ||
                child.getNode().getElementType() == RobotTokenTypes.TEST_CASES_HEADER)) {
                break;
            }

            // Extract keyword names
            if (inKeywordsSection && child.getNode().getElementType() == RobotTokenTypes.IDENTIFIER) {
                String keywordName = child.getText().trim();
                if (!keywordName.isEmpty() && !keywordName.startsWith("[")) {
                    result.addElement(LookupElementBuilder.create(keywordName)
                        .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                        .withTypeText("User Keyword")
                        .withTailText(" (defined in this file)", true));
                }
            }
        }
    }

    private void addSectionHeaders(CompletionResultSet result) {
        String[] headers = {
            "*** Settings ***", "*** Variables ***",
            "*** Test Cases ***", "*** Keywords ***", "*** Comments ***"
        };

        for (String header : headers) {
            result.addElement(LookupElementBuilder.create(header)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Tag)
                .withTypeText("Section Header"));
        }
    }
}
