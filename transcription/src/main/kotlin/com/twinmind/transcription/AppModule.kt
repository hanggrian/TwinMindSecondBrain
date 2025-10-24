package com.twinmind.transcription

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.room.Room
import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.db.TwinMindDb
import com.twinmind.transcription.rest.OpenAiApi.Companion.TIMEOUT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext context: Context,
    ): TwinMindDb =
        Room
            .databaseBuilder(context, TwinMindDb::class.java, TwinMindDb.NAME)
            .build()

    @Provides
    @Singleton
    fun provideChunks(db: TwinMindDb): Chunks = db.chunks()

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context,
    ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        useAlternativeNames = true
                        ignoreUnknownKeys = true
                        encodeDefaults = false
                    },
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT
                connectTimeoutMillis = TIMEOUT
                socketTimeoutMillis = TIMEOUT
            }
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            Log.v(TwinMindApp.TAG, message)
                        }
                    }
                level = LogLevel.ALL
            }
            install(ResponseObserver) {
                onResponse {
                    Log.d(TwinMindApp.TAG, "${it.status.value}")
                }
            }
            install(DefaultRequest) {
                header(
                    HttpHeaders.Authorization,
                    "Bearer ${BuildConfig.OPENAI_API_KEY}",
                )
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
}
