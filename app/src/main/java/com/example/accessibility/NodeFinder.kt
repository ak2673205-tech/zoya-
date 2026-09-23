package com.example.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object NodeFinder {

    fun findNodeByText(root: AccessibilityNodeInfo?, query: String): AccessibilityNodeInfo? {
        if (root == null || query.isBlank()) return null

        val cleanQuery = query.trim()

        // Try exact/contained match in root
        val text = root.text?.toString()
        val desc = root.contentDescription?.toString()

        if ((text != null && text.contains(cleanQuery, ignoreCase = true)) ||
            (desc != null && desc.contains(cleanQuery, ignoreCase = true))
        ) {
            return findClickableTarget(root)
        }

        // Recursively inspect children
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findNodeByText(child, cleanQuery)
            if (found != null) {
                return found
            }
        }
        return null
    }

    fun findNodeById(root: AccessibilityNodeInfo?, viewId: String): AccessibilityNodeInfo? {
        if (root == null || viewId.isBlank()) return null

        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        if (!nodes.isNullOrEmpty()) {
            return findClickableTarget(nodes.first())
        }
        return null
    }

    fun findFocusedEditableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null

        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            return focused
        }

        return findAnyEditableNode(root)
    }

    private fun findAnyEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findAnyEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun findClickableTarget(node: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return node
    }

    fun extractAllVisibleTexts(root: AccessibilityNodeInfo?, output: MutableList<String>) {
        if (root == null) return

        val text = root.text?.toString()?.trim()
        val desc = root.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty()) {
            output.add(text)
        } else if (!desc.isNullOrEmpty()) {
            output.add("[$desc]")
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            extractAllVisibleTexts(child, output)
        }
    }
}
