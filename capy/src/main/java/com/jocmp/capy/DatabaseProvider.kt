package com.jocmp.capy

import com.jocmp.capy.db.Database

interface DatabaseProvider {
    fun build(accountID: String): Database

    fun delete(accountID: String)
}
