package com.jocmp.basilreader.ui.accounts

import android.content.Context
import com.jocmp.basil.Account
import com.jocmp.basilreader.OPMLExporter

fun Context.exportOPML(account: Account) {
    OPMLExporter(this).export(account)
}
