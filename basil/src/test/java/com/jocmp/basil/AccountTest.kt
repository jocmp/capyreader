package com.jocmp.basil

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertContains

class AccountTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    @Test
    fun opmlFile_endsWithSubscriptions() {
        val accountPath = folder.newFile().toURI()

        val account = Account(id = "777", path = accountPath)

        assertContains(account.opmlFile.path.toString(), Regex("/subscriptions.opml$"))
    }
}
