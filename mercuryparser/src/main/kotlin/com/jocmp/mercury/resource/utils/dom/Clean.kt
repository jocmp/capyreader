package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc

private fun cleanComments(doc: Doc): Doc {
    doc.root().removeComments()
    return doc
}

fun clean(doc: Doc): Doc {
    doc(TAGS_TO_REMOVE).remove()
    return cleanComments(doc)
}
