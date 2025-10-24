package com.twinmind.transcription

import com.google.common.truth.Truth.assertThat
import com.twinmind.transcription.ai.Agent
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_LOW
import com.twinmind.transcription.ai.FakeAgent
import com.twinmind.transcription.ai.GoogleAgent
import com.twinmind.transcription.db.ChunkStatus
import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.db.schema.Chunk
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
@DoNotInstrument
@OptIn(ExperimentalCoroutinesApi::class)
class AgentTest {
    @Mock private lateinit var loading: MutableStateFlow<Boolean>

    @Mock private lateinit var error: MutableStateFlow<String>

    @Inject lateinit var chunks: Chunks

    @get:Rule var hiltRule = HiltAndroidRule(this)

    private lateinit var mocks: AutoCloseable

    @BeforeTest
    fun setup() {
        hiltRule.inject()
        mocks = MockitoAnnotations.openMocks(this)

        runBlocking(Dispatchers.IO) {
            repeat(5) { chunks.insert(Chunk(filePath = "", order = it)) }
        }
    }

    @AfterTest
    @Throws(Exception::class)
    fun cleanup() {
        runBlocking(Dispatchers.IO) {
            chunks.getAll().forEach { chunks.delete(it) }
        }

        mocks.close()
    }

    @Test
    fun `Transcribe with fake agent`() {
        val (collection, summary) =
            runBlocking(Dispatchers.IO) {
                val results = chunks.getAll()
                results to FakeAgent().transcribe(results, DETAIL_LOW, loading, error)
            }
        collection.forEach {
            assertThat(it.status).isEqualTo(ChunkStatus.TRANSCRIBED)
            assertThat(it.transcript).isNotNull()
        }
        assertThat(summary).isNotEmpty()
    }
}
