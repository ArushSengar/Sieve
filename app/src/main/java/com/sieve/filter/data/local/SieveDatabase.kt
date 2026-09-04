package com.sieve.filter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sieve.filter.data.local.dao.AppRuleDao
import com.sieve.filter.data.local.dao.BlockLogDao
import com.sieve.filter.data.local.dao.KeywordRuleDao
import com.sieve.filter.data.local.entity.AppRuleEntity
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.model.RuleAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppRuleEntity::class,
        KeywordRuleEntity::class,
        BlockLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SieveDatabase : RoomDatabase() {

    abstract fun appRuleDao(): AppRuleDao
    abstract fun keywordRuleDao(): KeywordRuleDao
    abstract fun blockLogDao(): BlockLogDao

    companion object {
        @Volatile
        private var INSTANCE: SieveDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SieveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SieveDatabase::class.java,
                    "sieve_database"
                )
                    .addCallback(SieveDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_BLOCK_KEYWORDS = listOf(
            "% off",
            "cashback",
            "flash sale",
            "limited time",
            "discount",
            "buy 1 get 1",
            "bogo",
            "flat ₹",
            "flat $",
            "coupon code",
            "use code",
            "sale live",
            "mega sale",
            "hurry up",
            "offer expires"
        )

        val DEFAULT_ALLOW_KEYWORDS = listOf(
            "delivered",
            "out for delivery",
            "otp",
            "order confirmed",
            "arriving",
            "on the way",
            "picked up",
            "driver arriving",
            "security code",
            "verification code",
            "transit",
            "dispatched"
        )
    }

    private class SieveDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultKeywords(database.keywordRuleDao())
                }
            }
        }

        suspend fun populateDefaultKeywords(dao: KeywordRuleDao) {
            val rules = mutableListOf<KeywordRuleEntity>()

            // Add default block keywords (global)
            DEFAULT_BLOCK_KEYWORDS.forEach { pattern ->
                rules.add(
                    KeywordRuleEntity(
                        packageName = null,
                        pattern = pattern,
                        action = RuleAction.BLOCK.name
                    )
                )
            }

            // Add default allow keywords (global)
            DEFAULT_ALLOW_KEYWORDS.forEach { pattern ->
                rules.add(
                    KeywordRuleEntity(
                        packageName = null,
                        pattern = pattern,
                        action = RuleAction.ALLOW.name
                    )
                )
            }

            dao.insertAll(rules)
        }
    }
}
