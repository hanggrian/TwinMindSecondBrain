package com.twinmind.transcription

import android.content.Context
import android.media.AudioManager
import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.db.TwinMindDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext context: Context,
    ): TwinMindDb = TwinMindDb.from(context)

    @Provides
    @Singleton
    fun provideChunk(db: TwinMindDb): Chunks = db.chunks()

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context,
    ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}
