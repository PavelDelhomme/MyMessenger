package com.delhomme.mymessenger.di

import com.delhomme.mymessenger.data.local.AppDatabase
import com.delhomme.mymessenger.data.repository.ConversationRepository
import com.delhomme.mymessenger.data.repository.MessageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideMessageRepository(database: AppDatabase): MessageRepository {
        return MessageRepository(database)
    }

    @Provides
    @Singleton
    fun provideConversationRepository(database: AppDatabase): ConversationRepository {
        return ConversationRepository(database)
    }
}
