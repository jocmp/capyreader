package com.jocmp.basil

import com.jocmp.basil.db.Database

interface DatabaseProvider {
    fun forAccount(accountID: String): Database
}
