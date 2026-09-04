package com.sieve.filter

import android.app.Application
import com.sieve.filter.data.local.PreferencesManager
import com.sieve.filter.data.local.SieveDatabase
import com.sieve.filter.data.repository.SieveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class providing application-wide singletons for Room database and SieveRepository.
 * Automatically triggers background log pruning to prevent database bloat and conserve flash wear.
 */
class SieveApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy {
        SieveDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        SieveRepository(
            appRuleDao = database.appRuleDao(),
            keywordRuleDao = database.keywordRuleDao(),
            blockLogDao = database.blockLogDao(),
            aiSuggestedRuleDao = database.aiSuggestedRuleDao(),
            scope = applicationScope
        )
    }

    val preferencesManager by lazy {
        PreferencesManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Background log retention auto-pruning to keep Room database compact
        applicationScope.launch(Dispatchers.IO) {
            try {
                val retentionDays = preferencesManager.logRetentionDays.value
                if (retentionDays > 0) {
                    repository.pruneOldLogs(retentionDays)
                }
            } catch (_: Exception) {}
        }
    }

    companion object {
        lateinit var instance: SieveApplication
            private set
    }
}
