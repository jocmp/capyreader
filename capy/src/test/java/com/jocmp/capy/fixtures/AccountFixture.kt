package com.jocmp.capy.fixtures

import com.jocmp.capy.Account
import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.InMemoryDataStore
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.db.Database
import io.mockk.mockk
import org.junit.rules.TemporaryFolder
import java.io.File

object AccountFixture {
    fun create(
        id: String = RandomUUID.generate(),
        parentFolder: TemporaryFolder,
        database: Database = InMemoryDatabaseProvider.build(id),
        accountDelegate: AccountDelegate = mockk()
    ): Account {
        return Account(
            id = id,
            path = parentFolder.newFile().toURI(),
            database = database,
            cacheDirectory = File(parentFolder.root, "cache"),
            preferences = AccountPreferences(InMemoryDataStore()),
            delegate = accountDelegate
        )
    }
}
