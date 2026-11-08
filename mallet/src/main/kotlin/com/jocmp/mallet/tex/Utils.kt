package com.jocmp.mallet.tex

private val characterNodesTypes = setOf("mathord", "textord", "atom")

/** The innermost node of a group with a single element, looking through ordgroup, color and font. */
fun getBaseElem(group: ParseNode): ParseNode =
    when {
        group is ParseNode.OrdGroup && group.body.size == 1 -> getBaseElem(group.body[0])
        group is ParseNode.Color && group.body.size == 1 -> getBaseElem(group.body[0])
        group is ParseNode.Font -> getBaseElem(group.body)
        else -> group
    }

fun isCharacterBox(group: ParseNode): Boolean = getBaseElem(group).type in characterNodesTypes
