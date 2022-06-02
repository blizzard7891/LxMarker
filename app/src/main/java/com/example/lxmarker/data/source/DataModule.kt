package com.example.lxmarker.data.source

import android.content.Context
import androidx.room.Room
import com.example.lxmarker.data.source.local.CheckInDao
import com.example.lxmarker.data.source.local.LxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {
    @Singleton
    @Provides
    fun provideLxDatabase(
        @ApplicationContext context: Context
    ): LxDatabase = Room.databaseBuilder(context, LxDatabase::class.java, "lx_marker")
        .fallbackToDestructiveMigration()
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Singleton
    @Provides
    fun provideCheckInDao(lxDatabase: LxDatabase): CheckInDao = lxDatabase.checkInDao()
}