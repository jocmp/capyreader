package com.jocmp.basilreader

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.jocmp.basil.AccountPreferences
import com.jocmp.basil.PreferencesProvider
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class AccountPreferencesProvider(
    val serializer: AccountPreferencesSerializer,
    val context: Context
) : PreferencesProvider {
    override fun forAccount(accountID: String): DataStore<AccountPreferences> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.dataStoreFile("account_$accountID") }
        )
    }
}

class AccountPreferencesSerializer : Serializer<AccountPreferences> {
    override val defaultValue = AccountPreferences(displayName = "")

    override suspend fun readFrom(input: InputStream): AccountPreferences =
        try {
            Json.decodeFromString(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: AccountPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
