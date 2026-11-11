/*
 * Copyright (c) 2026 Jakub Jirák.
 * Licensed under the Apache License, Version 2.0. See LICENSE for details.
 */
package com.jakubjirak.robotframework.feedback

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Non-intrusive feedback prompt shown once after 7 days of plugin usage.
 * Tracks first activation timestamp and shows a dismissible balloon notification.
 */
@Service(Service.Level.APP)
@State(name = "com.jakubjirak.robotframework.FeedbackState", storages = [Storage("com-jakubjirak-robotframework-feedback.xml")])
class FeedbackState : PersistentStateComponent<FeedbackState.State> {
    data class State(
        var firstActivation: Long = 0L,
        var feedbackShown: Boolean = false,
        var dismissed: Boolean = false
    )

    private var myState = State()
    override fun getState(): State = myState
    override fun loadState(state: State) { myState = state }

    companion object {
        fun getInstance(): FeedbackState = service()
    }
}

class FeedbackNotifier : ProjectActivity {
    override suspend fun execute(project: Project) {
        val state = FeedbackState.getInstance().state
        if (!shouldShowFeedback(state, System.currentTimeMillis())) return
        state.feedbackShown = true

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "Robot Framework Professional",
                "You've been using this plugin for over a week — thank you! 🎉\nWe'd love your feedback to help us improve.",
                NotificationType.INFORMATION
            )
            .addAction(NotificationAction.createSimpleExpiring("📧 Send Feedback") {
                BrowserUtil.browse("mailto:feedback@jakubjirak.com?subject=Feedback: Robot%20Framework%20Professional")
            })
            .addAction(NotificationAction.createSimpleExpiring("⭐ Rate on Marketplace") {
                BrowserUtil.browse("https://plugins.jetbrains.com/plugin/29046/reviews")
            })
            .addAction(NotificationAction.createSimpleExpiring("Later") {
                state.feedbackShown = false
            })
            .addAction(NotificationAction.createSimpleExpiring("Don't show again") {
                state.dismissed = true
            })

        notification.notify(project)
    }

    companion object {
        /** Must match the `notificationGroup` id registered in plugin.xml. */
        const val NOTIFICATION_GROUP_ID: String = "Robot Framework Feedback"

        /** Days of usage before the one-off feedback balloon is offered. */
        const val DAYS_BEFORE_PROMPT: Long = 7
        private val DAY_MS: Long = java.util.concurrent.TimeUnit.DAYS.toMillis(1)

        /**
         * Decides whether the feedback balloon should be shown at [now].
         * The first call only records the activation time (and never shows the prompt).
         */
        fun shouldShowFeedback(state: FeedbackState.State, now: Long): Boolean {
            if (state.firstActivation == 0L) {
                state.firstActivation = now
                return false
            }
            if (state.feedbackShown || state.dismissed) return false
            return (now - state.firstActivation) / DAY_MS >= DAYS_BEFORE_PROMPT
        }
    }
}
