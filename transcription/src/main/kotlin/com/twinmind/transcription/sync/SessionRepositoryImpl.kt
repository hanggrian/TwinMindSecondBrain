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

        private object PreferencesKeys {
            val IS_RECORDING = booleanPreferencesKey("is_recording")
            val STATUS = stringPreferencesKey("status")
            val PAUSE_REASON = stringPreferencesKey("pause_reason")
            val GRACEFUL_TERMINATION = stringPreferencesKey("graceful_termination")
        }

        override suspend fun update(session: Session) {
            context.dataStore.edit {
                it[PreferencesKeys.IS_RECORDING] = session.isRecording
                it[PreferencesKeys.STATUS] = session.state.name
                it[PreferencesKeys.PAUSE_REASON] = session.pauseReason.name
                it[PreferencesKeys.GRACEFUL_TERMINATION] = session.gracefulTermination.name
            }
        }

        override fun getSessionFlow(): Flow<Session> =
            context.dataStore.data
                .catch {
                    if (it is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw it
                    }
                }.map {
                    Session(
                        it[PreferencesKeys.IS_RECORDING] ?: false,
                        State.valueOf(it[PreferencesKeys.STATUS] ?: State.IDLE.name),
                        PauseReason
                            .valueOf(it[PreferencesKeys.PAUSE_REASON] ?: PauseReason.NONE.name),
                        GracefulTermination.valueOf(
                            it[PreferencesKeys.GRACEFUL_TERMINATION]
                                ?: GracefulTermination.DEFAULT.name,
                        ),
                    )
                }
    }
