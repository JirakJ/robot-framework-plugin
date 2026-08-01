package com.jakubjirak.robotframework.editor;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jakubjirak.robotframework.analysis.RobotSyntax;
import com.jakubjirak.robotframework.language.RobotLanguage;
import com.jakubjirak.robotframework.language.RobotTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Code completion for Robot Framework 7.4: control structures including
 * {@code GROUP} (RF 7.2), current suite/test/keyword settings, {@code VAR}
 * scopes including {@code SUITES} (RF 7.1), {@code WHILE} limit options, and
 * context-aware completion of conversion types inside {@code ${name: type}}
 * (RF 7.3).
 */
public class RobotCompletionContributor extends CompletionContributor {

    /** Caret sits after the type colon of a variable, e.g. {@code ${count: |}}. */
    private static final Pattern INSIDE_TYPE =
            Pattern.compile(".*[$@&]\\{[^{}]*:\\s[^{}]*$", Pattern.DOTALL);

    public RobotCompletionContributor() {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(RobotLanguage.INSTANCE),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                            @NotNull ProcessingContext context,
                                            @NotNull CompletionResultSet result) {

                    String prefix = linePrefix(parameters);

                    // Inside ${name: ...} only a type makes sense.
                    if (INSIDE_TYPE.matcher(prefix).matches()) {
                        addConversionTypes(result);
                        return;
                    }

                    addControlStructures(result);
                    addStatementOptions(prefix, result);
                    addBuiltInKeywords(result);
                    addCommonVariables(result);
                    addSettings(result);
                    addSettingsSectionKeywords(result);
                    addVariablesFromFile(parameters, result);
                    addKeywordsFromFile(parameters, result);
                    addSectionHeaders(result);
                }
            });
    }

    /** Text of the current line up to the caret. */
    private static String linePrefix(CompletionParameters parameters) {
        PsiFile file = parameters.getOriginalFile();
        String text = file.getText();
        int offset = Math.max(0, Math.min(parameters.getOffset(), text.length()));
        int start = text.lastIndexOf('\n', Math.max(0, offset - 1)) + 1;
        return text.substring(start, offset);
    }

    private void addConversionTypes(CompletionResultSet result) {
        for (String type : RobotSyntax.CONVERSION_TYPES) {
            result.addElement(LookupElementBuilder.create(type)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Class)
                .withTypeText("Conversion type")
                .withTailText(" (Robot Framework 7.3+)", true));
        }
    }

    private void addControlStructures(CompletionResultSet result) {
        String[] markers = {
            "IF", "ELSE IF", "ELSE", "END",
            "TRY", "EXCEPT", "FINALLY",
            "WHILE", "FOR", "GROUP", "BREAK", "CONTINUE",
            "RETURN", "VAR"
        };

        for (String marker : markers) {
            LookupElementBuilder element = LookupElementBuilder.create(marker)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Method)
                .withTypeText("Control structure")
                .withBoldness(true);
            if ("GROUP".equals(marker)) {
                element = element.withTailText(" (Robot Framework 7.2+)", true);
            } else if ("VAR".equals(marker)) {
                element = element.withTailText(" (Robot Framework 7.0+)", true);
            }
            result.addElement(element);
        }
    }

    /** Offers {@code scope=} values after VAR and limit options after WHILE. */
    private void addStatementOptions(String prefix, CompletionResultSet result) {
        String statement = RobotSyntax.normalizeMarker(prefix.trim());

        if (statement.startsWith("VAR")) {
            for (String scope : RobotSyntax.VAR_SCOPES) {
                result.addElement(LookupElementBuilder.create("scope=" + scope)
                    .withIcon(com.intellij.icons.AllIcons.Nodes.Parameter)
                    .withTypeText("VAR scope")
                    .withTailText("SUITES".equals(scope) ? " (Robot Framework 7.1+)" : "", true));
            }
            result.addElement(LookupElementBuilder.create("separator=")
                .withIcon(com.intellij.icons.AllIcons.Nodes.Parameter)
                .withTypeText("VAR option"));
        }

        if (statement.startsWith("WHILE")) {
            for (String option : RobotSyntax.WHILE_OPTIONS) {
                result.addElement(LookupElementBuilder.create(option)
                    .withIcon(com.intellij.icons.AllIcons.Nodes.Parameter)
                    .withTypeText("WHILE option"));
            }
        }
    }

    private void addBuiltInKeywords(CompletionResultSet result) {
        // BuiltIn library keywords. Keywords deprecated in favour of control
        // structures (Exit For Loop, Return From Keyword, ...) are omitted on
        // purpose so completion never suggests syntax the user should migrate off.
        String[] builtInKeywords = {
            "Log", "Log To Console", "Log Many", "Log Variables",
            "Should Be Equal", "Should Not Be Equal", "Should Contain", "Should Not Contain",
            "Should Be True", "Should Be False", "Should Be Empty", "Should Not Be Empty",
            "Should Match", "Should Not Match", "Should Match Regexp", "Should Not Match Regexp",
            "Should Start With", "Should Not Start With", "Should End With", "Should Not End With",
            "Set Variable", "Set Variable If", "Set Test Variable", "Set Task Variable",
            "Set Suite Variable", "Set Global Variable", "Set Local Variable",
            "Create List", "Create Dictionary", "Append To List", "Remove From List", "Get From List",
            "Set To Dictionary", "Get From Dictionary", "Remove From Dictionary",
            "Get Dictionary Keys", "Get Dictionary Values",
            "Run Keyword", "Run Keyword If", "Run Keywords",
            "Run Keyword And Continue On Failure", "Run Keyword And Expect Error",
            "Run Keyword And Ignore Error", "Run Keyword And Warn On Failure",
            "Wait Until Keyword Succeeds", "Repeat Keyword", "Run Keyword And Return",
            "Run Keyword And Return If", "Run Keyword And Return Status",
            "Pass Execution", "Pass Execution If", "Fail", "Fatal Error", "Skip", "Skip If",
            "Get Length", "Get Count", "Length Should Be",
            "Should Be Equal As Integers", "Should Be Equal As Numbers", "Should Be Equal As Strings",
            "Convert To Integer", "Convert To Number", "Convert To String", "Convert To Boolean",
            "Convert To Bytes", "Convert To Hex", "Convert To Octal", "Convert To Binary",
            "Sleep", "Catenate", "Get Time", "Evaluate",
            "Import Library", "Import Resource", "Import Variables",
            "Set Tags", "Remove Tags", "Get Library Instance", "Reload Library",
            "No Operation", "Comment"
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
        for (String variable : RobotSyntax.BUILT_IN_VARIABLES) {
            result.addElement(LookupElementBuilder.create(variable)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Variable)
                .withTypeText("Built-in Variable"));
        }
    }

    private void addSettings(CompletionResultSet result) {
        for (String setting : RobotSyntax.TEST_SETTINGS) {
            result.addElement(LookupElementBuilder.create(setting)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Annotationtype)
                .withTypeText("Test setting"));
        }
        for (String setting : RobotSyntax.KEYWORD_SETTINGS) {
            if (RobotSyntax.TEST_SETTINGS.contains(setting)) {
                continue;
            }
            result.addElement(LookupElementBuilder.create(setting)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Annotationtype)
                .withTypeText("Keyword setting")
                .withTailText("[Setup]".equals(setting) ? " (Robot Framework 7.0+)" : "", true));
        }
    }

    private void addSettingsSectionKeywords(CompletionResultSet result) {
        for (String setting : RobotSyntax.SUITE_SETTINGS) {
            result.addElement(LookupElementBuilder.create(setting)
                .withIcon(com.intellij.icons.AllIcons.General.Settings)
                .withTypeText("Settings Section")
                .withTailText(" (*** Settings ***)", true));
        }
    }

    private void addVariablesFromFile(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile file = parameters.getOriginalFile();

        boolean inVariablesSection = false;

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            if (child.getNode().getElementType() == RobotTokenTypes.VARIABLES_HEADER) {
                inVariablesSection = true;
                continue;
            }

            if (inVariablesSection
                    && RobotTokenTypes.SECTION_HEADERS.contains(child.getNode().getElementType())) {
                break;
            }

            if (inVariablesSection && child.getNode().getElementType() == RobotTokenTypes.VARIABLE) {
                RobotSyntax.TypedVariable variable = RobotSyntax.parseVariable(child.getText().trim());
                if (variable == null || variable.getName().isEmpty()) {
                    continue;
                }
                String decoration = child.getText().trim().substring(0, 1);
                String lookup = decoration + "{" + variable.getName() + "}";
                result.addElement(LookupElementBuilder.create(lookup)
                    .withIcon(com.intellij.icons.AllIcons.Nodes.Variable)
                    .withTypeText(variable.isTyped() ? variable.getType() : "File Variable")
                    .withTailText(" (defined in this file)", true));
            }
        }
    }

    private void addKeywordsFromFile(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile file = parameters.getOriginalFile();

        boolean inKeywordsSection = false;

        for (PsiElement child : file.getChildren()) {
            if (child.getNode() == null) continue;

            if (child.getNode().getElementType() == RobotTokenTypes.KEYWORDS_HEADER) {
                inKeywordsSection = true;
                continue;
            }

            if (inKeywordsSection
                    && RobotTokenTypes.SECTION_HEADERS.contains(child.getNode().getElementType())) {
                break;
            }

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
            "*** Settings ***", "*** Variables ***", "*** Test Cases ***",
            "*** Tasks ***", "*** Keywords ***", "*** Comments ***"
        };

        for (String header : headers) {
            result.addElement(LookupElementBuilder.create(header)
                .withIcon(com.intellij.icons.AllIcons.Nodes.Tag)
                .withTypeText("Section Header"));
        }
    }
}
