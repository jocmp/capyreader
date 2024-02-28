package com.jocmp.basil.fixtures

import com.jocmp.basil.Account
import com.jocmp.basil.AccountPreferences
import com.jocmp.basil.InMemoryDataStore
import com.jocmp.basil.InMemoryDatabaseProvider
import com.jocmp.basil.RandomUUID
import org.junit.rules.TemporaryFolder

object AccountFixture {
    fun create(
        id: String = RandomUUID.generate(),
        parentFolder: TemporaryFolder,
    ): Account {
        val database = InMemoryDatabaseProvider.build(id)

        return Account(
            id = id,
            path = parentFolder.newFile().toURI(),
            database = database,
            preferences = AccountPreferences(InMemoryDataStore()),
        )
    }
}
