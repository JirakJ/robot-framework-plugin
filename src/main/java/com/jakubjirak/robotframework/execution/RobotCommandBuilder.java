/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the {@code robot} command line as a plain argument list.
 *
 * <p>Kept free of IDE and process APIs so the exact arguments can be asserted in
 * unit tests. Arguments are never concatenated into a shell string — the caller
 * hands the list to {@code GeneralCommandLine}, which executes without a shell,
 * so a test name or a path containing spaces, quotes or semicolons cannot turn
 * into a second command.</p>
 */
public final class RobotCommandBuilder {

    /** Everything the command line depends on. */
    public static final class Params {
        private String interpreterPath = "";
        private String suitePath = "";
        private String testName = "";
        private String includeTags = "";
        private String excludeTags = "";
        private String outputDirectory = "";
        private String extraArguments = "";
        private boolean debugLogLevel;

        public Params interpreterPath(@Nullable String value) {
            this.interpreterPath = nullToEmpty(value);
            return this;
        }

        public Params suitePath(@Nullable String value) {
            this.suitePath = nullToEmpty(value);
            return this;
        }

        public Params testName(@Nullable String value) {
            this.testName = nullToEmpty(value);
            return this;
        }

        public Params includeTags(@Nullable String value) {
            this.includeTags = nullToEmpty(value);
            return this;
        }

        public Params excludeTags(@Nullable String value) {
            this.excludeTags = nullToEmpty(value);
            return this;
        }

        public Params outputDirectory(@Nullable String value) {
            this.outputDirectory = nullToEmpty(value);
            return this;
        }

        public Params extraArguments(@Nullable String value) {
            this.extraArguments = nullToEmpty(value);
            return this;
        }

        /** Adds {@code --loglevel DEBUG} so the log carries every keyword argument. */
        public Params debugLogLevel(boolean value) {
            this.debugLogLevel = value;
            return this;
        }

        public String getSuitePath() {
            return suitePath;
        }

        public String getTestName() {
            return testName;
        }
    }

    /** Interpreter used when the configuration and the project settings are both empty. */
    public static final String DEFAULT_INTERPRETER =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("win")
                    ? "python" : "python3";

    private RobotCommandBuilder() {
    }

    /**
     * Returns the full argument list, executable first.
     *
     * <p>Robot Framework is started as {@code <interpreter> -m robot} rather than
     * through the {@code robot} launcher script, because the launcher is often
     * missing from a virtualenv that the IDE knows about.</p>
     */
    public static List<String> build(Params params) {
        List<String> command = new ArrayList<>();
        command.add(params.interpreterPath.isEmpty() ? DEFAULT_INTERPRETER : params.interpreterPath);
        command.add("-m");
        command.add("robot");

        // Keeps the run console readable and colored inside the IDE.
        command.add("--consolecolors");
        command.add("ansi");

        if (params.debugLogLevel) {
            command.add("--loglevel");
            command.add("DEBUG");
        }
        if (!params.outputDirectory.isEmpty()) {
            command.add("--outputdir");
            command.add(params.outputDirectory);
        }
        if (!params.testName.isEmpty()) {
            command.add("--test");
            command.add(params.testName);
        }
        for (String tag : splitTags(params.includeTags)) {
            command.add("--include");
            command.add(tag);
        }
        for (String tag : splitTags(params.excludeTags)) {
            command.add("--exclude");
            command.add(tag);
        }
        command.addAll(splitArguments(params.extraArguments));

        if (!params.suitePath.isEmpty()) {
            command.add(params.suitePath);
        }
        return command;
    }

    /** Splits a comma or whitespace separated tag list. */
    public static List<String> splitTags(String tags) {
        List<String> result = new ArrayList<>();
        for (String part : tags.split("[,\\s]+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Splits a free-form argument string the way a shell would group quoted
     * words, without ever handing the string to a shell.
     */
    public static List<String> splitArguments(String arguments) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inWord = false;
        char quote = 0;

        for (int i = 0; i < arguments.length(); i++) {
            char c = arguments.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                } else {
                    current.append(c);
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                quote = c;
                inWord = true;
                continue;
            }
            if (Character.isWhitespace(c)) {
                if (inWord) {
                    result.add(current.toString());
                    current.setLength(0);
                    inWord = false;
                }
                continue;
            }
            current.append(c);
            inWord = true;
        }
        if (inWord) {
            result.add(current.toString());
        }
        return result;
    }

    /** Human readable form of the command, for logging and error messages. */
    public static String describe(List<String> command) {
        return String.join(" ", command);
    }

    private static String nullToEmpty(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
