package com.jocmp.basil

import com.jocmp.basil.db.Database

interface DatabaseProvider {
    fun build(accountID: String): Database

    fun delete(accountID: String)
}
