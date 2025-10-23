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
import com.twinmind.transcription.ai.Agent
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_HIGH
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_LOW
import com.twinmind.transcription.ai.MockAgent
import com.twinmind.transcription.res.DIMEN_LARGE
import com.twinmind.transcription.res.DIMEN_MEDIUM
import com.twinmind.transcription.res.DIMEN_SMALL
import com.twinmind.transcription.sync.MainService
import com.twinmind.transcription.sync.MainService.Companion.ACTION_RESUME
import com.twinmind.transcription.sync.MainService.Companion.ACTION_START
import com.twinmind.transcription.sync.MainService.Companion.ACTION_STOP
import com.twinmind.transcription.sync.State
import kotlinx.coroutines.launch

val DETAIL_LEVELS = listOf("Low", "Medium", "High")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val loading by viewModel.loadingFlow.collectAsState()
    val summary by viewModel.summaryFlow.collectAsState()
    val error by viewModel.errorFlow.collectAsState()
    val session by viewModel.sessionFlow.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var detail by remember { mutableFloatStateOf(DETAIL_LOW) }
    var agent by remember { mutableStateOf(MockAgent.name) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var wasActive by remember { mutableStateOf(false) } // hotfix for sheet appearing on startup

    LaunchedEffect(session.state, summary) {
        if (session.state == State.RECORDING || session.state == State.PAUSED) {
            wasActive = true
        }

        if (session.state == State.STOPPED) {
            if (wasActive) {
                viewModel.startTranscription(agent, detail)
                if (summary.isNotBlank()) {
                    showBottomSheet = true
                    wasActive = false
                }
            }
        } else {
            showBottomSheet = false
        }
    }
    LaunchedEffect(error) {
        error.takeUnless { it.isEmpty() }?.let {
            snackbarState.showSnackbar(message = it)
            viewModel.errorFlow.value = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                        modifier = Modifier.padding(DIMEN_MEDIUM),
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
                                .size(48.dp)
                                .padding(end = DIMEN_MEDIUM),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(DIMEN_LARGE),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Detail:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(96.dp),
                )
                Slider(
                    value = detail,
                    onValueChange = { detail = it },
                    valueRange = DETAIL_LOW..DETAIL_HIGH,
                    steps = 1,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = DIMEN_SMALL, bottom = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Agent:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(96.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Agent.all().forEach { a ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = agent == a.name,
                                        onClick = { agent = a.name },
                                        enabled = a.isEnabled,
                                    ),
                        ) {
                            RadioButton(
                                selected = agent == a.name,
                                onClick = { agent = a.name },
                                enabled = a.isEnabled,
                            )
                            Text(
                                text = a.name,
                                color =
                                    LocalContentColor.current.takeIf { a.isEnabled }
                                        ?: LocalContentColor.current.copy(alpha = 0.38f),
                            )
                        }
                    }
                }
            }
            RecordButton()
            Text(
                text =
                    when (session.state) {
                        State.IDLE -> "Oh, hi there!"
                        State.RECORDING -> "Recording..."
                        State.PAUSED -> "Paused."
                        else -> "Done."
                    },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = DIMEN_LARGE),
            )
        }

        if (loading) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (!showBottomSheet) {
            return@Scaffold
        }
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            TopAppBar(
                title = {
                    Text("Transcription")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
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
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close summary",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
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
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(DIMEN_LARGE),
            ) {
                LabelText("Summary")
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = DIMEN_MEDIUM),
                )
                LabelText("Options")
                Text(
                    text = "Agent: $agent",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = DIMEN_MEDIUM),
                )
                Text(
                    text = "Detail Level: ${DETAIL_LEVELS[detail.toInt()]}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = DIMEN_SMALL),
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
        onClick = {
            Intent(context, MainService::class.java).let {
                it.action = action
                if (action == ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "Record button",
            modifier = Modifier.size(DIMEN_LARGE),
        )
    }
}

@Composable
private fun LabelText(text: String) =
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = DIMEN_MEDIUM),
    )
