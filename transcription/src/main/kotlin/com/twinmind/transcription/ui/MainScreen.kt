package com.twinmind.transcription.ui

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makeramen.roundedimageview.RoundedImageView
import com.twinmind.transcription.R
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_HIGH
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_LOW
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_MEDIUM
import com.twinmind.transcription.ai.FakeAgent
import com.twinmind.transcription.res.Dimens
import com.twinmind.transcription.sync.MainService
import com.twinmind.transcription.sync.MainService.Companion.ACTION_RESUME
import com.twinmind.transcription.sync.MainService.Companion.ACTION_START
import com.twinmind.transcription.sync.MainService.Companion.ACTION_STOP
import com.twinmind.transcription.sync.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val loading by viewModel.loadingFlow.collectAsState()
    val summary by viewModel.summaryFlow.collectAsState()
    val transcription by viewModel.transcriptionFlow.collectAsState()
    val error by viewModel.errorFlow.collectAsState()
    val elapsed by viewModel.elapsedTimeFlow.collectAsState("00:00")
    val session by viewModel.sessionFlow.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var detail by remember { mutableFloatStateOf(DETAIL_LOW) }
    var agent by remember { mutableStateOf(FakeAgent.NAME) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var wasActive by remember { mutableStateOf(false) } // hotfix for sheet appearing on startup

    LaunchedEffect(session.state, transcription) {
        if (session.state == State.RECORDING || session.state == State.PAUSED) {
            wasActive = true
        }

        if (session.state == State.STOPPED) {
            if (wasActive) {
                scope.launch(Dispatchers.IO) {
                    viewModel.startTranscription(agent, detail)
                    scope.launch {
                        if (transcription.isBlank()) {
                            return@launch
                        }
                        showBottomSheet = true
                        wasActive = false
                    }
                }
            }
        } else {
            showBottomSheet = false
        }
    }
    LaunchedEffect(error) {
        error.takeUnless { it.isEmpty() }?.let {
            snackbarState.showSnackbar(it)
            viewModel.errorFlow.value = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                {},
                navigationIcon = {
                    Image(
                        painter =
                            painterResource(
                                id =
                                    R.drawable.ic_launcher_dark
                                        .takeIf { darkTheme }
                                        ?: R.drawable.ic_launcher_light,
                            ),
                        contentDescription = "TwinMind icon",
                        modifier = Modifier.padding(Dimens.Medium),
                    )
                },
                actions = {
                    AndroidView(
                        factory = {
                            RoundedImageView(it)
                                .apply {
                                    setImageResource(R.drawable.pic_profile)
                                    contentDescription = "Profile picture"
                                    cornerRadius = 128.dp.value
                                }
                        },
                        modifier =
                            Modifier
                                .size(Dimens.XLarge)
                                .padding(end = Dimens.Medium),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.Large),
            Arrangement.Center,
            Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                Text(
                    "Timer:",
                    Modifier.width(Dimens.XXXLarge),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    elapsed,
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = Dimens.Small),
                    MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Medium, bottom = Dimens.Medium),
                Arrangement.Center,
                Alignment.CenterVertically,
            ) {
                Text(
                    "AI agent:",
                    Modifier.width(Dimens.XXXLarge),
                    style = MaterialTheme.typography.titleMedium,
                )
                Column(Modifier.weight(1f)) {
                    viewModel.agents.forEach { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    agent == a.name,
                                    a.isEnabled(),
                                    onClick = { agent = a.name },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                agent == a.name,
                                { agent = a.name },
                                enabled = a.isEnabled(),
                            )
                            Text(
                                a.name,
                                color =
                                    LocalContentColor.current.takeIf { a.isEnabled() }
                                        ?: LocalContentColor.current.copy(alpha = 0.38f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                Text(
                    "Summary detail:",
                    Modifier.width(96.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                Slider(
                    detail,
                    { detail = it },
                    Modifier.weight(1f),
                    valueRange = DETAIL_LOW..DETAIL_HIGH,
                    steps = 1,
                )
            }
            RecordButton()
            Text(
                when (session.state) {
                    State.IDLE -> "Oh, hi there!"
                    State.RECORDING -> "Recording..."
                    State.PAUSED -> "Paused."
                    else -> "Done."
                },
                Modifier.padding(top = Dimens.Large),
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        if (loading) {
            Dialog(onDismissRequest = {}) {
                Box(
                    Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Dimens.Small),
                        ),
                    Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (!showBottomSheet) {
            return@Scaffold
        }
        ModalBottomSheet({ showBottomSheet = false }, sheetState = sheetState) {
            TopAppBar(
                {
                    Column {
                        Text(agent, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "$elapsed (${
                                when (detail) {
                                    DETAIL_LOW -> "Low"
                                    DETAIL_MEDIUM -> "Medium"
                                    else -> "High"
                                }
                            })",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                        },
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_close),
                            "Close summary",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    IconButton(
                        {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, summary)
                                        type = "text/plain"
                                    },
                                    null,
                                ),
                            )
                        },
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_share),
                            "Share",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.Large),
            ) {
                LabelText("Summary")
                Text(
                    summary,
                    Modifier.padding(bottom = Dimens.Medium),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LabelText("Transcription")
                Text(
                    transcription,
                    Modifier.padding(bottom = Dimens.Medium),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun RecordButton(viewModel: MainViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val session by viewModel.sessionFlow.collectAsStateWithLifecycle()

    val action: String
    val iconId: Int
    when (session.state) {
        State.RECORDING -> {
            action = ACTION_STOP
            iconId = R.drawable.ic_stop
        }
        State.PAUSED -> {
            action = ACTION_RESUME
            iconId = R.drawable.ic_play
        }
        else -> {
            action = ACTION_START
            iconId = R.drawable.ic_record
        }
    }

    LargeFloatingActionButton(
        {
            Intent(context, MainService::class.java).let {
                it.action = action
                if (action == ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
                }
            }
        },
        Modifier.padding(top = Dimens.XXXLarge),
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Icon(painterResource(id = iconId), "Record button", Modifier.size(Dimens.Large))
    }
}

@Composable
private fun LabelText(text: String) =
    Text(
        text,
        Modifier.padding(bottom = Dimens.Medium),
        MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
