package com.example.livecui

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper

class AccessibilityBridge(host: View) : ExploreByTouchHelper(host) {
    
    data class VirtualNode(val id: Int, val label: String, val bounds: Rect)
    
    val nodes = mutableListOf<VirtualNode>()

    override fun getVirtualViewAt(x: Float, y: Float): Int {
        for (node in nodes) {
            if (node.bounds.contains(x.toInt(), y.toInt())) {
                return node.id
            }
        }
        return HOST_ID
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        for (node in nodes) {
            virtualViewIds.add(node.id)
        }
    }

    override fun onPopulateNodeForVirtualView(
        virtualViewId: Int,
        node: AccessibilityNodeInfoCompat
    ) {
        val virtualNode = nodes.find { it.id == virtualViewId }
        if (virtualNode != null) {
            node.contentDescription = virtualNode.label
            node.className = "android.widget.Button"
            node.setBoundsInParent(virtualNode.bounds)
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
        } else {
            node.contentDescription = ""
            node.setBoundsInParent(Rect(0, 0, 1, 1))
        }
    }

    override fun onPerformActionForVirtualView(
        virtualViewId: Int,
        action: Int,
        arguments: Bundle?
    ): Boolean {
        if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
            // Trigger event klik C-UI di sini
            return true
        }
        return false
    }
}
