package com.dev.headphoneplayer.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dev.headphoneplayer.ui.theme.HeadphonePlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import com.dev.headphoneplayer.R
import com.dev.headphoneplayer.ui.component.*
import com.dev.headphoneplayer.ui.theme.Black3
import com.dev.headphoneplayer.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel by viewModels<MainViewModel>()

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                mainViewModel.state.isLoading
            }
        }

        setContent {

            val sheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )

            val scope = rememberCoroutineScope()

            val state = mainViewModel.state

            val context = LocalContext.current

            val screenHeight = screenHeight()

            val dialogText = stringResource(id = R.string.txt_permissions)

            val errorText = stringResource(id = R.string.txt_error_app_settings)

            requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    val permissionsGranted = permissions.values.reduce { acc, next -> acc && next }
                    if (!permissionsGranted) {
                        showPermissionsRationalDialog(
                            context = context,
                            okButtonTextResId = R.string.lbl_ok,
                            cancelButtonTextResId = R.string.lbl_cancel,
                            dialogText = dialogText,
                            errorText = errorText,
                            packageName = packageName
                        )
                    }
                }
            )

            HeadphonePlayerTheme {
                ModalBottomSheetLayout(
                    sheetState = sheetState,
                    sheetShape = MaterialTheme.shapes.large,
                    sheetContent = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(state = rememberScrollState())
                                .padding(top = 16.dp)
                        ) {
                            if (state.audios.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WarningMessage(
                                        text = stringResource(id = R.string.txt_no_media),
                                        iconResId = R.drawable.circle_info_solid,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.lbl_tracks),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.h3,
                                        modifier = Modifier.padding(bottom = 3.dp, top = 12.dp),
                                        textDecoration = TextDecoration.Underline,
                                        color = MaterialTheme.colors.onBackground
                                    )
                                }
                                state.audios.forEach { audio ->
                                    Track(
                                        audio = audio,
                                        isPlaying = audio.songId == state.selectedAudio.songId,
                                        modifier = Modifier
                                            .padding(
                                                horizontal = 8.dp, vertical = 10.dp
                                            )
                                            .requiredHeight(height = 100.dp),
                                        onClick = {
                                            scope.launch {
                                                mainViewModel.onEvent(event = AudioPlayerEvent.Stop)
                                                sheetState.hide()
                                                mainViewModel.onEvent(event = AudioPlayerEvent.InitAudio(
                                                    audio = it,
                                                    context = context,
                                                    onAudioInitialized = {
                                                        mainViewModel.onEvent(event = AudioPlayerEvent.Play)
                                                    }
                                                ))
                                            }
                                        }
                                    )
                                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }
                    },
                    content = {
                        LoadingDialog(
                            isLoading = state.isLoading,
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.large)
                                .background(color = MaterialTheme.colors.surface)
                                .requiredSize(size = 80.dp),
                            onDone = { mainViewModel.onEvent(event = AudioPlayerEvent.HideLoadingDialog) }
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(state = rememberScrollState())
                                .background(color = MaterialTheme.colors.background),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TopBar(
                                modifier = Modifier
                                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                    .requiredHeight(height = 80.dp),
                                leadingIcon = {
                                    LikeButton(
                                        isLiked = state.likedSongs.contains(state.selectedAudio.songId),
                                        enabled = state.selectedAudio.isNotEmpty(),
                                        onClick = {
                                            mainViewModel.onEvent(
                                                event = AudioPlayerEvent.LikeOrNotSong(
                                                    id = state.selectedAudio.songId
                                                )
                                            )
                                        }
                                    )
                                },
                                title = {
                                    if (state.selectedAudio.isNotEmpty()) {
                                        val artist = if (state.selectedAudio.artist.contains(
                                                "unknown",
                                                ignoreCase = true
                                            )
                                        ) "" else "${state.selectedAudio.artist} - "
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(text = artist)
                                                }
                                                append(text = "  ${state.selectedAudio.songTitle}")
                                            },
                                            color = MaterialTheme.colors.onSurface,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.h3
                                        )
                                    }
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        setupPermissions(
                                            context = context,
                                            permissions = arrayOf(
                                                Manifest.permission.RECORD_AUDIO,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                            ),
                                            launcher = requestPermissionLauncher,
                                            onPermissionsGranted = {
                                                scope.launch {
                                                    if (state.audios.isEmpty()) {
                                                        mainViewModel.onEvent(event = AudioPlayerEvent.LoadMedias)
                                                    }
                                                    sheetState.show()
                                                }
                                            }
                                        )
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.up_right_from_square_solid),
                                            contentDescription = "",
                                            tint = MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.requiredHeight(height = 16.dp))

                            state.selectedAudio.cover?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    modifier = Modifier
                                        .requiredHeight(height = screenHeight * 0.4f)
                                        .clip(shape = MaterialTheme.shapes.large),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = ""
                                )
                            } ?: Box(
                                modifier = Modifier.requiredHeight(height = screenHeight * 0.4f),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    elevation = 8.dp,
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxHeight(fraction = 0.5f)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.musical_note_music_svgrepo_com),
                                        modifier = Modifier
                                            .padding(
                                                top = 25.dp,
                                                bottom = 26.dp,
                                                start = 8.dp,
                                                end = 20.dp
                                            ),
                                        contentScale = ContentScale.FillHeight,
                                        contentDescription = ""
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.requiredHeight(height = 16.dp))

                            StackedBarVisualizer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height = 200.dp)
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                shape = MaterialTheme.shapes.large,
                                barCount = 32,
                                barColors = listOf(
                                    Color(0xFF1BEBE9),
                                    Color(0xFF39AFEA),
                                    Color(0xFF0291D8)
                                ),
                                stackBarBackgroundColor = if (isSystemInDarkTheme()) Black3 else
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.25f),
                                data = mainViewModel.visualizerData.value
                            )

                            Spacer(modifier = Modifier.requiredHeight(height = 10.dp))

                            TimeBar(
                                currentPosition = state.currentPosition,
                                onValueChange = { position ->
                                    mainViewModel.onEvent(event = AudioPlayerEvent.Seek(position = position))
                                },
                                duration = state.selectedAudio.duration
                            )

                            Spacer(modifier = Modifier.requiredHeight(height = 10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                FastButton(
                                    enabled = state.currentPosition > FORWARD_BACKWARD_STEP,
                                    onClick = {
                                        mainViewModel.onEvent(
                                            event = AudioPlayerEvent.Seek(position = state.currentPosition - FORWARD_BACKWARD_STEP.toFloat())
                                        )
                                    },
                                    iconResId = R.drawable.backward_solid,
                                    stringResId = R.string.lbl_fast_backward
                                )
                                PlayPauseButton(
                                    modifier = Modifier.padding(horizontal = 26.dp),
                                    enabled = state.selectedAudio.isNotEmpty(),
                                    isPlaying = state.isPlaying,
                                    onPlay = { mainViewModel.onEvent(event = AudioPlayerEvent.Play) },
                                    onPause = { mainViewModel.onEvent(event = AudioPlayerEvent.Pause) }
                                )
                                FastButton(
                                    enabled = state.currentPosition < (state.selectedAudio.duration - FORWARD_BACKWARD_STEP),
                                    onClick = {
                                        mainViewModel.onEvent(
                                            event = AudioPlayerEvent.Seek(position = state.currentPosition + FORWARD_BACKWARD_STEP.toFloat())
                                        )
                                    },
                                    iconResId = R.drawable.forward_solid,
                                    stringResId = R.string.lbl_fast_forward
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
