package com.twinmind.transcription

import com.twinmind.transcription.ai.Agent
import com.twinmind.transcription.ai.FakeAgent
import com.twinmind.transcription.ai.GoogleAgent
import com.twinmind.transcription.ai.OpenAiAgent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentModule {
    @Binds
    @IntoMap
    @StringKey(OpenAiAgent.NAME)
    abstract fun bindOpenAiAgent(agent: OpenAiAgent): Agent

    @Binds
    @IntoMap
    @StringKey(GoogleAgent.NAME)
    abstract fun bindGoogleAgent(agent: GoogleAgent): Agent

    @Binds
    @IntoMap
    @StringKey(FakeAgent.NAME)
    abstract fun bindFakeAgent(agent: FakeAgent): Agent
}
