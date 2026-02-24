package com.rootilabs.wmeCardiac.ui.main

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rootilabs.wmeCardiac.R
import com.rootilabs.wmeCardiac.ui.theme.*
import com.rootilabs.wmeCardiac.ui.history.HistoryScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onViewHistory: () -> Unit,
    onViewProfile: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) onLogout()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Speech Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = data?.get(0) ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.setOtherSymptom(spokenText)
            }
        }
    }

    val startVoiceInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "請點選麥克風後說出您的症狀")
        }
        speechRecognizerLauncher.launch(intent)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF757575)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF616161))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(id = R.string.system_settings), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Personal Profile Item
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable {
                            scope.launch { drawerState.close() }
                            onViewProfile()
                        },
                    color = TagGoGreen,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(2.dp, TagGoGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.profile_icon_normal),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.personal_profile), color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Green toolbar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TagGoGreen)
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        Text(
                            text = stringResource(id = R.string.digital_tagging),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Main content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFE8E8E8)), // Restored: Original Light Gray
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Current time
                        val currentTime = remember { mutableStateOf("") }
                        val currentDate = remember { mutableStateOf("") }
                        LaunchedEffect(Unit) {
                            var counter = 0
                            while (true) {
                                val now = Date()
                                currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                                currentDate.value = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
                                
                                // Refresh status and sync tags every 10 seconds
                                if (counter % 10 == 0) {
                                    viewModel.checkRecordingStatus() 
                                    viewModel.loadEventTags() // Sync status update
                                }
                                
                                counter++
                                kotlinx.coroutines.delay(1000)
                            }
                        }

                        Text(
                            text = currentTime.value,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.W300,
                            color = Color(0xFF424242)
                        )
                        Text(
                            text = currentDate.value,
                            fontSize = 24.sp, // Even larger
                            color = Color(0xFF616161),
                            modifier = Modifier.padding(top = 2.dp)
                        )


                        // Status text
                        Text(
                            text = if (uiState.isMeasuring) stringResource(id = R.string.tap_to_tag) else stringResource(id = R.string.no_recording),
                            fontSize = 24.sp,
                            color = Color(0xFF757575), // Grey as per screenshot
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Tag button with Pointer
                        TagButton(
                            isMeasuring = uiState.isMeasuring,
                            onClick = { viewModel.onTagPressed() },
                            modifier = Modifier.offset(y = (-10).dp)
                        )

                        // Device Image - 靠近按鈕尖角
                        Image(
                            painter = painterResource(id = R.drawable.img_rx),
                            contentDescription = "Device",
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(1.2f)
                                .offset(y = (-20).dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Bottom bar (Footer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF888888))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = R.string.last_tag), color = Color(0xFFE0E0E0), fontSize = 14.sp)
                            Text(
                                text = uiState.lastTagTime ?: stringResource(id = R.string.no_records),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box {
                            Button(
                                onClick = onViewHistory,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(stringResource(id = R.string.view), color = Color.White, fontSize = 16.sp)
                            }

                            if (uiState.showSyncErrorBadge) {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_warning2),
                                    contentDescription = "Sync Error",
                                    modifier = Modifier
                                        .size(26.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                )
                            }
                        }
                    }
                }
            }


            // Tag Flow Overlay
            if (uiState.tagFlowStep != TagFlowStep.IDLE) {
                TagFlowOverlay(
                    viewModel = viewModel,
                    onStartVoiceInput = startVoiceInput
                )
            }
        }
    }
}

@Composable
fun TagButton(isMeasuring: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    var isFlashing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    val circleColor by animateColorAsState(
        targetValue = if (isFlashing) Color(0xFFB2EBF2) else Color(0xFF80DEEA),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = if (isFlashing) 50 else 100),
        label = "circleColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(300.dp)
    ) {
        // Blue circle - only shown when measuring, flashes on tap
        if (isMeasuring) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(circleColor, CircleShape)
            )
        }

        // Main image - switches based on recording state
        Image(
            painter = painterResource(
                id = when {
                    !isMeasuring          -> R.drawable.btn_notready
                    isPressed || isFlashing -> R.drawable.btn_pressed
                    else                  -> R.drawable.btn_standby
                }
            ),
            contentDescription = "Tag",
            modifier = Modifier
                .size(290.dp)
                .scale(scale)
                .pointerInput(isMeasuring) {
                    if (isMeasuring) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                scope.launch {
                                    if (vibrator.hasVibrator()) {
                                        val pattern = longArrayOf(0, 200, 100, 200, 100, 400)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator.vibrate(pattern, -1)
                                        }
                                    }

                                    // Triple flash - only the blue circle
                                    isFlashing = true
                                    kotlinx.coroutines.delay(200)
                                    isFlashing = false
                                    kotlinx.coroutines.delay(100)

                                    isFlashing = true
                                    kotlinx.coroutines.delay(200)
                                    isFlashing = false
                                    kotlinx.coroutines.delay(100)

                                    isFlashing = true
                                    kotlinx.coroutines.delay(400)
                                    isFlashing = false

                                    onClick()
                                }
                            }
                        )
                    }
                },
            contentScale = ContentScale.Fit
        )
    }
}

private val TriangleShape = object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}
