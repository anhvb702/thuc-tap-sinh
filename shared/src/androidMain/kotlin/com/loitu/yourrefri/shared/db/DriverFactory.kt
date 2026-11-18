package com.loitu.yourrefri.shared.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** DB trên máy mới hơn schema của app: báo cho UI, không hạ cấp, không xóa. */
class DatabaseTooNewException(val storedVersion: Long, val appVersion: Long) :
    RuntimeException("Database version $storedVersion is newer than app schema $appVersion")

class DriverFactory(private val context: Context) {
    private val dbName = "yourrefri.db"

    fun create(): SqlDriver = AndroidSqliteDriver(YourRefriDatabase.Schema, context, dbName)

    /** `PRAGMA user_version` của file đang có; null khi chưa có file (lần cài đầu). Mở chỉ đọc. */
    fun storedVersion(): Long? {
        val file = context.getDatabasePath(dbName)
        if (!file.exists()) return null
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("PRAGMA user_version", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        }
    }

    /** Ném [DatabaseTooNewException] nếu DB trên máy mới hơn app; không đụng file khi ổn. */
    fun ensureOpenable() {
        val stored = storedVersion() ?: return
        val app = YourRefriDatabase.Schema.version
        if (isDatabaseTooNew(stored, app)) throw DatabaseTooNewException(stored, app)
    }
}
