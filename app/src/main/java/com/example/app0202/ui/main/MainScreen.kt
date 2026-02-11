package com.example.app0202.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app0202.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onViewHistory: () -> Unit = {},
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

    // Side drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // ... (no changes here for now)
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF757575)
            ) {
                // Drawer header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF616161))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("系統設定", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (uiState.loginTimeDisplay.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "登入時間: ${uiState.loginTimeDisplay}",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Logout button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                    color = Color(0xFF9E9E9E),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("➡\uFE0F", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("登出帳號", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    ) {
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
                        text = "數位標註",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Main content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Current time
                        val currentTime = remember { mutableStateOf("") }
                        val currentDate = remember { mutableStateOf("") }
                        LaunchedEffect(Unit) {
                            while (true) {
                                val now = Date()
                                currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                                currentDate.value = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(now)
                                kotlinx.coroutines.delay(1000)
                            }
                        }

                        Text(
                            text = currentTime.value,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                        Text(
                            text = currentDate.value,
                            fontSize = 16.sp,
                            color = Color(0xFF757575)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status text
                        Text(
                            text = if (uiState.isMeasuring) "按一下按鍵來標註事件" else "您現在並沒有在錄製中！",
                            fontSize = 16.sp,
                            color = if (uiState.isMeasuring) Color(0xFF616161) else TagGoRed,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tag button
                        TagButton(
                            isMeasuring = uiState.isMeasuring,
                            onClick = { viewModel.onTagPressed() }
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Bottom bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TagGoBottomBar)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("上一次標註:", color = Color(0xFFBDBDBD), fontSize = 12.sp)
                        Text(
                            text = uiState.lastTagTime ?: "尚無紀錄",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = onViewHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("查看", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Tag flow overlay
    if (uiState.tagFlowStep != TagFlowStep.IDLE) {
        TagFlowOverlay(viewModel = viewModel)
    }
}

@Composable
fun TagButton(isMeasuring: Boolean, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val ringColor by animateColorAsState(
        targetValue = when {
            !isMeasuring -> TagGoRed
            isPressed -> TagGoDarkCyan
            else -> TagGoCyan
        },
        label = "ringColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .shadow(if (isPressed) 12.dp else 6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(8.dp, ringColor, CircleShape)
                .pointerInput(isMeasuring) {
                    if (isMeasuring) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Tag icon
            Text("📍", fontSize = 48.sp)
        }

        // Pin triangle
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(16.dp)
                .background(
                    color = ringColor,
                    shape = TriangleShape
                )
        )
    }
}

// Simple triangle shape for the pin
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
