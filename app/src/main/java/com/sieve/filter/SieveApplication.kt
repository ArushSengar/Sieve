package com.sieve.filter

import android.app.Application
import com.sieve.filter.data.local.SieveDatabase
import com.sieve.filter.data.repository.SieveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Application class providing application-wide singletons for Room database and SieveRepository.
 */
class SieveApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        SieveDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        SieveRepository(
            appRuleDao = database.appRuleDao(),
            keywordRuleDao = database.keywordRuleDao(),
            blockLogDao = database.blockLogDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SieveApplication
            private set
    }
}
