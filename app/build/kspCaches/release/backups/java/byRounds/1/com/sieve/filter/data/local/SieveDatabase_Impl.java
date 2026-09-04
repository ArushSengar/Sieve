package com.sieve.filter.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.sieve.filter.data.local.dao.AppRuleDao;
import com.sieve.filter.data.local.dao.AppRuleDao_Impl;
import com.sieve.filter.data.local.dao.BlockLogDao;
import com.sieve.filter.data.local.dao.BlockLogDao_Impl;
import com.sieve.filter.data.local.dao.KeywordRuleDao;
import com.sieve.filter.data.local.dao.KeywordRuleDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SieveDatabase_Impl extends SieveDatabase {
  private volatile AppRuleDao _appRuleDao;

  private volatile KeywordRuleDao _keywordRuleDao;

  private volatile BlockLogDao _blockLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_rules` (`package_name` TEXT NOT NULL, `mode` TEXT NOT NULL, PRIMARY KEY(`package_name`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `keyword_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `package_name` TEXT, `pattern` TEXT NOT NULL, `action` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `block_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `package_name` TEXT NOT NULL, `title` TEXT, `text_snippet` TEXT, `channel_id` TEXT, `matched_rule` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a55c52e849d5ee7ea48dcee64dfb6d7b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `app_rules`");
        db.execSQL("DROP TABLE IF EXISTS `keyword_rules`");
        db.execSQL("DROP TABLE IF EXISTS `block_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAppRules = new HashMap<String, TableInfo.Column>(2);
        _columnsAppRules.put("package_name", new TableInfo.Column("package_name", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppRules.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppRules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppRules = new TableInfo("app_rules", _columnsAppRules, _foreignKeysAppRules, _indicesAppRules);
        final TableInfo _existingAppRules = TableInfo.read(db, "app_rules");
        if (!_infoAppRules.equals(_existingAppRules)) {
          return new RoomOpenHelper.ValidationResult(false, "app_rules(com.sieve.filter.data.local.entity.AppRuleEntity).\n"
                  + " Expected:\n" + _infoAppRules + "\n"
                  + " Found:\n" + _existingAppRules);
        }
        final HashMap<String, TableInfo.Column> _columnsKeywordRules = new HashMap<String, TableInfo.Column>(4);
        _columnsKeywordRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywordRules.put("package_name", new TableInfo.Column("package_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywordRules.put("pattern", new TableInfo.Column("pattern", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywordRules.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKeywordRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKeywordRules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKeywordRules = new TableInfo("keyword_rules", _columnsKeywordRules, _foreignKeysKeywordRules, _indicesKeywordRules);
        final TableInfo _existingKeywordRules = TableInfo.read(db, "keyword_rules");
        if (!_infoKeywordRules.equals(_existingKeywordRules)) {
          return new RoomOpenHelper.ValidationResult(false, "keyword_rules(com.sieve.filter.data.local.entity.KeywordRuleEntity).\n"
                  + " Expected:\n" + _infoKeywordRules + "\n"
                  + " Found:\n" + _existingKeywordRules);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsBlockLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("package_name", new TableInfo.Column("package_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("text_snippet", new TableInfo.Column("text_snippet", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("channel_id", new TableInfo.Column("channel_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("matched_rule", new TableInfo.Column("matched_rule", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockLogs = new TableInfo("block_logs", _columnsBlockLogs, _foreignKeysBlockLogs, _indicesBlockLogs);
        final TableInfo _existingBlockLogs = TableInfo.read(db, "block_logs");
        if (!_infoBlockLogs.equals(_existingBlockLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "block_logs(com.sieve.filter.data.local.entity.BlockLogEntity).\n"
                  + " Expected:\n" + _infoBlockLogs + "\n"
                  + " Found:\n" + _existingBlockLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a55c52e849d5ee7ea48dcee64dfb6d7b", "a4127497692a763cdd581fe495cee152");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "app_rules","keyword_rules","block_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `app_rules`");
      _db.execSQL("DELETE FROM `keyword_rules`");
      _db.execSQL("DELETE FROM `block_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AppRuleDao.class, AppRuleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(KeywordRuleDao.class, KeywordRuleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BlockLogDao.class, BlockLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AppRuleDao appRuleDao() {
    if (_appRuleDao != null) {
      return _appRuleDao;
    } else {
      synchronized(this) {
        if(_appRuleDao == null) {
          _appRuleDao = new AppRuleDao_Impl(this);
        }
        return _appRuleDao;
      }
    }
  }

  @Override
  public KeywordRuleDao keywordRuleDao() {
    if (_keywordRuleDao != null) {
      return _keywordRuleDao;
    } else {
      synchronized(this) {
        if(_keywordRuleDao == null) {
          _keywordRuleDao = new KeywordRuleDao_Impl(this);
        }
        return _keywordRuleDao;
      }
    }
  }

  @Override
  public BlockLogDao blockLogDao() {
    if (_blockLogDao != null) {
      return _blockLogDao;
    } else {
      synchronized(this) {
        if(_blockLogDao == null) {
          _blockLogDao = new BlockLogDao_Impl(this);
        }
        return _blockLogDao;
      }
    }
  }
}
