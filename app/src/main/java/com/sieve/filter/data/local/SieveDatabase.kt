package com.sieve.filter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.sieve.filter.data.local.dao.AiSuggestedRuleDao
import com.sieve.filter.data.local.dao.AppRuleDao
import com.sieve.filter.data.local.dao.BlockLogDao
import com.sieve.filter.data.local.dao.KeywordRuleDao
import com.sieve.filter.data.local.dao.PaymentFlagLogDao
import com.sieve.filter.data.local.entity.AiSuggestedRuleEntity
import com.sieve.filter.data.local.entity.AppRuleEntity
import com.sieve.filter.data.local.entity.BlockLogEntity
import com.sieve.filter.data.local.entity.KeywordRuleEntity
import com.sieve.filter.data.local.entity.PaymentFlagLogEntity
import com.sieve.filter.model.RuleAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppRuleEntity::class,
        KeywordRuleEntity::class,
        BlockLogEntity::class,
        AiSuggestedRuleEntity::class,
        PaymentFlagLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SieveDatabase : RoomDatabase() {

    abstract fun appRuleDao(): AppRuleDao
    abstract fun keywordRuleDao(): KeywordRuleDao
    abstract fun blockLogDao(): BlockLogDao
    abstract fun aiSuggestedRuleDao(): AiSuggestedRuleDao
    abstract fun paymentFlagLogDao(): PaymentFlagLogDao

    companion object {
        @Volatile
        private var INSTANCE: SieveDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ai_suggested_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `suggestedKeyword` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `sampleTitle` TEXT,
                        `sampleText` TEXT,
                        `status` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_suggested_rules_status` ON `ai_suggested_rules` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_suggested_rules_packageName_suggestedKeyword` ON `ai_suggested_rules` (`packageName`, `suggestedKeyword`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Extend block_logs with stage_id and matched_pattern_id for "Explain this block"
                db.execSQL("ALTER TABLE `block_logs` ADD COLUMN `stage_id` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `block_logs` ADD COLUMN `matched_pattern_id` TEXT DEFAULT NULL")

                // 2. Extend app_rules with quiet hours settings
                db.execSQL("ALTER TABLE `app_rules` ADD COLUMN `quiet_hours_enabled` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `app_rules` ADD COLUMN `quiet_hours_start_minutes` INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE `app_rules` ADD COLUMN `quiet_hours_end_minutes` INTEGER NOT NULL DEFAULT -1")

                // 3. Create payment_flag_log for suspicious collect request advisory history
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `payment_flag_log` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `package_name` TEXT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `dismissed_by_user` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_flag_log_sender` ON `payment_flag_log` (`sender`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_flag_log_package_name` ON `payment_flag_log` (`package_name`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): SieveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SieveDatabase::class.java,
                    "sieve_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
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
            "offer expires",
            "deal",
            "deals",
            "price drop",
            "refer & earn",
            "refer and earn",
            "exclusive deal",
            "lowest price",
            "side hustle",
            "earns between payments",
            "gold price",
            "digital gold",
            "save in gold",
            "daily savings",
            "apna gullak",
            "gullak",
            "spin to win",
            "spin & win",
            "spin the wheel",
            "scratch & win",
            "scratch and win",
            "claim reward",
            "unclaimed reward",
            "free gold",
            "earn interest",
            "pre-approved loan",
            "pre-approved credit",
            "instant loan",
            "pocket money",
            "पॉकेट मनी",
            "कैशबैक",
            "वाउचर",
            "रिचार्ज",
            "बिल पेमेंट",
            "ऑफर",
            "छूट",
            "बचत",
            "कमाएं",
            "जीतें",
            "पाएं"
        )

        val DEFAULT_ALLOW_KEYWORDS = listOf(
            "order delivered",
            "has been delivered",
            "package delivered",
            "successfully delivered",
            "out for delivery",
            "otp",
            "order confirmed",
            "order placed",
            "order prepared",
            "order update",
            "arriving",
            "on the way",
            "picked up",
            "driver arriving",
            "rider arriving",
            "captain arriving",
            "ride pin",
            "ride confirmed",
            "security code",
            "verification code",
            "in transit",
            "dispatched",
            "refund initiated",
            "refund processed",
            "return pickup",
            "paid to",
            "paid you",
            "sent you",
            "transferred to",
            "credited to",
            "credited with",
            "debited from",
            "payment received",
            "received from",
            "money sent",
            "money received",
            "bhim upi",
            "upi ref",
            "upi reference",
            "acct ending",
            "a/c ending",
            "txn id",
            "transaction successful",
            "payment successful",
            "रुपये भेजे गए",
            "रुपये प्राप्त हुए",
            "खाते से काटे गए",
            "खाते में जमा"
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
