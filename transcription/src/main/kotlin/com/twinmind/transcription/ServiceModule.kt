package com.twinmind.transcription

import com.twinmind.transcription.sync.NotificationRepository
import com.twinmind.transcription.sync.NotificationRepositoryImpl
import com.twinmind.transcription.sync.RecordingRepository
import com.twinmind.transcription.sync.RecordingRepositoryImpl
import com.twinmind.transcription.sync.SessionRepository
import com.twinmind.transcription.sync.SessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    @Singleton
    abstract fun bindRecordingRepository(impl: RecordingRepositoryImpl): RecordingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl,
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
