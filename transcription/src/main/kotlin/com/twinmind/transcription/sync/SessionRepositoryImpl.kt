package com.twinmind.transcription.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SessionRepository {
        private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "recording_session")

        override val flow: Flow<Session>
            get() =
                context.dataStore.data
                    .catch {
                        if (it is IOException) {
                            emit(emptyPreferences())
                        } else {
                            throw it
                        }
                    }.map {
                        Session(
                            it[IS_RECORDING] ?: false,
                            State.valueOf(it[STATUS] ?: State.IDLE.name),
                            it[ELAPSED_TIME] ?: 0L,
                            PauseReason.valueOf(it[PAUSE_REASON] ?: PauseReason.NONE.name),
                            GracefulTermination.valueOf(
                                it[GRACEFUL_TERMINATION]
                                    ?: GracefulTermination.DEFAULT.name,
                            ),
                        )
                    }

        override suspend fun update(session: Session) {
            context.dataStore.edit {
                it[IS_RECORDING] = session.isRecording
                it[STATUS] = session.state.name
                it[ELAPSED_TIME] = session.elapsedTime
                it[PAUSE_REASON] = session.pauseReason.name
                it[GRACEFUL_TERMINATION] = session.gracefulTermination.name
            }
        }

        override suspend fun updateElapsedTime(elapsedTime: Long) {
            context.dataStore.edit {
                it[ELAPSED_TIME] = elapsedTime
            }
        }

        private companion object {
            val IS_RECORDING = booleanPreferencesKey("is_recording")
            val STATUS = stringPreferencesKey("status")
            val ELAPSED_TIME = longPreferencesKey("elapsed_time")
            val PAUSE_REASON = stringPreferencesKey("pause_reason")
            val GRACEFUL_TERMINATION = stringPreferencesKey("graceful_termination")
        }
    }
