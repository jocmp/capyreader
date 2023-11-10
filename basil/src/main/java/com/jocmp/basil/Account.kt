package com.jocmp.basil

import android.content.Context
import com.jocmp.basil.opml.Folder
import com.jocmp.basil.opml.Outline
import java.io.File

class Account(context: Context, id: String) {
    var folders: MutableSet<Folder> = mutableSetOf()
        private set

    fun loadOPMLItems(items: List<Outline>) {
        items.forEach { item ->
            when (item) {
                is Outline.FolderOutline -> folders.add(item.asFolder)
                is Outline.FeedOutline -> print("Feed for ya")
            }
        }
    }
}

private val Outline.FolderOutline.asFolder: Folder
    get() {
        return Folder(title = folder.title)
    }