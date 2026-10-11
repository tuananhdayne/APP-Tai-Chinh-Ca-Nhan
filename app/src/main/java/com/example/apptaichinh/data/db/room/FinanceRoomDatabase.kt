package com.example.apptaichinh.data.db.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room owns the existing finance_app.db schema. The current helper remains a
 * compatibility adapter while CRUD is moved incrementally to typed DAOs.
 */
@Database(
    entities = [CategoryEntity::class, TransactionEntity::class, SettingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FinanceRoomDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        const val DATABASE_NAME = "finance_app.db"
    }
}
