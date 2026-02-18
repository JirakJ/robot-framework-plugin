package com.jakubjirak.robotframework.editor

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jakubjirak.robotframework.language.RobotTokenTypes

/**
 * Provides parameter name hints for Robot Framework keyword calls
 * using the stable declarative inlay hints API.
 */
class RobotParameterHintsProvider : InlayHintsProvider {

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector {
        return Collector()
    }

    private class Collector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            val node = element.node ?: return
            if (node.elementType != RobotTokenTypes.KEYWORD) return

            var sibling = element.nextSibling
            var argIndex = 0

            while (sibling != null) {
                if (sibling.node?.elementType == RobotTokenTypes.IDENTIFIER) {
                    val argText = sibling.text.trim()
                    if ("=" !in argText && argText.isNotEmpty()) {
                        val hintText = "arg$argIndex:"
                        @Suppress("DEPRECATION")
                        sink.addPresentation(
                            InlineInlayPosition(sibling.textRange.startOffset, false),
                            null,
                            null,
                            true
                        ) {
                            text(hintText)
                        }
                        argIndex++
                    }
                }
                sibling = sibling.nextSibling
            }
        }
    }
}
