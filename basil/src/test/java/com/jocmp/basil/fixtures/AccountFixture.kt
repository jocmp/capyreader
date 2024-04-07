package com.jocmp.basil.fixtures

import com.jocmp.basil.Account
import com.jocmp.basil.AccountDelegate
import com.jocmp.basil.AccountPreferences
import com.jocmp.basil.InMemoryDataStore
import com.jocmp.basil.InMemoryDatabaseProvider
import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import io.mockk.mockk
import org.junit.rules.TemporaryFolder

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
            preferences = AccountPreferences(InMemoryDataStore()),
            delegate = accountDelegate
        )
    }
}
