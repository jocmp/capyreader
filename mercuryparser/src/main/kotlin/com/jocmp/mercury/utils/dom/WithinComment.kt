package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Selection

fun withinComment(node: Selection): Boolean {
    val parents = node.elements.firstOrNull()?.parents() ?: return false
    val commentParent =
        parents.find { parent ->
            val attrs = getAttrs(parent)
            val nodeClass = attrs["class"] ?: ""
            val id = attrs["id"] ?: ""
            val classAndId = "$nodeClass $id"
            classAndId.contains("comment")
        }
    return commentParent != null
}
