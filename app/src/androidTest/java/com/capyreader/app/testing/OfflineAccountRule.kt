package com.capyreader.app.testing

import com.capyreader.app.loadAccountModules
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.unloadAccountModules
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.db.Database
import org.junit.rules.ExternalResource
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

class OfflineAccountRule(
    private val filter: ArticleFilter = ArticleFilter.default(),
    private val markReadOnScroll: Boolean = false,
) : ExternalResource() {
    lateinit var delegate: OfflineAccountDelegate
        private set

    private val koin
        get() = GlobalContext.get()

    private val appPreferences
        get() = koin.get<AppPreferences>()

    private val accountManager
        get() = koin.get<AccountManager>()

    private var accountID = ""
    private var previousAccountID = ""
    private var previousFilter: ArticleFilter = ArticleFilter.default()
    private var previousMarkReadOnScroll = false
    private var overrides: Module? = null

    override fun before() {
        previousAccountID = appPreferences.accountID.get()
        previousFilter = appPreferences.filter.get()
        previousMarkReadOnScroll = appPreferences.articleListOptions.markReadOnScroll.get()

        accountID = accountManager.createAccount(source = Source.LOCAL)

        appPreferences.accountID.set(accountID)
        appPreferences.filter.set(filter)
        appPreferences.articleListOptions.markReadOnScroll.set(markReadOnScroll)

        unloadAccountModules()
        loadAccountModules()

        val database = koin.get<Database>()
        val account = accountManager.findByID(id = accountID, database = database)!!
        account.preferences.lastRefreshedAt.set(nowUTC().toEpochSecond())

        delegate = OfflineAccountDelegate(database)

        val offlineAccount = account.copy(delegate = delegate)

        overrides = module {
            single<Account> { offlineAccount }
        }.also(::loadKoinModules)
    }

    override fun after() {
        overrides?.let(::unloadKoinModules)
        unloadAccountModules()

        accountManager.removeAccount(accountID)

        appPreferences.accountID.set(previousAccountID)
        appPreferences.filter.set(previousFilter)
        appPreferences.articleListOptions.markReadOnScroll.set(previousMarkReadOnScroll)

        if (previousAccountID.isNotBlank()) {
            loadAccountModules()
        }
    }
}
