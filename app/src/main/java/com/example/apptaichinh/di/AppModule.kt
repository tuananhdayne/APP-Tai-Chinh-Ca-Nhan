package com.example.apptaichinh.di

import android.content.Context
import com.example.apptaichinh.data.ai.AiService
import com.example.apptaichinh.data.db.FinanceDatabaseHelper
import com.example.apptaichinh.data.db.room.FinanceDao
import com.example.apptaichinh.data.db.room.FinanceRoomDatabase
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): FinanceRoomDatabase =
        Room.databaseBuilder(context, FinanceRoomDatabase::class.java, FinanceRoomDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideFinanceDao(database: FinanceRoomDatabase): FinanceDao = database.financeDao()

    @Provides
    @Singleton
    fun provideFinanceDatabaseHelper(@ApplicationContext context: Context): FinanceDatabaseHelper =
        FinanceDatabaseHelper.getInstance(context)

    @Provides
    @Singleton
    fun provideAiService(databaseHelper: FinanceDatabaseHelper): AiService = AiService(databaseHelper)
}
