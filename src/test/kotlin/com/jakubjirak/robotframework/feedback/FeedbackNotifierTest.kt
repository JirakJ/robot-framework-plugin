/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.feedback

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class FeedbackNotifierTest {

    private val day = TimeUnit.DAYS.toMillis(1)
    private val start = 1_700_000_000_000L

    @Test
    fun `first run records activation and does not prompt`() {
        val state = FeedbackState.State()
        assertFalse(FeedbackNotifier.shouldShowFeedback(state, start))
        assertEquals(start, state.firstActivation)
    }

    @Test
    fun `prompt waits a full week`() {
        val state = FeedbackState.State(firstActivation = start)
        assertFalse(FeedbackNotifier.shouldShowFeedback(state, start + 7 * day - 1))
        assertTrue(FeedbackNotifier.shouldShowFeedback(state, start + 7 * day))
    }

    @Test
    fun `never prompts again once shown or dismissed`() {
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(start, feedbackShown = true), start + 30 * day))
        assertFalse(FeedbackNotifier.shouldShowFeedback(FeedbackState.State(start, dismissed = true), start + 30 * day))
    }

    @Test
    fun `notification group id matches plugin xml`() {
        val pluginXml = javaClass.getResource("/META-INF/plugin.xml")!!.readText()
        assertTrue(
            pluginXml.contains("""<notificationGroup id="${FeedbackNotifier.NOTIFICATION_GROUP_ID}""""),
            "plugin.xml must register the feedback notification group"
        )
    }

    @Test
    fun `state component round-trips`() {
        val component = FeedbackState()
        val loaded = FeedbackState.State(firstActivation = 1L)
        component.loadState(loaded)
        assertSame(loaded, component.state)
    }
}
