package com.rootilabs.wmeCardiac.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rootilabs.wmeCardiac.R
import com.rootilabs.wmeCardiac.di.ServiceLocator
import com.rootilabs.wmeCardiac.ui.theme.TagGoGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val tokenManager = ServiceLocator.tokenManager
    val patientId = tokenManager.patientId ?: "---"
    val vendorName = tokenManager.vendorName ?: "---"
    
    var showLogoutDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) onLogoutSuccess()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("個人資料", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onBack) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp).border(1.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TagGoGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F6F9)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // ID Section
                ProfileInfoItem(
                    painter = painterResource(id = R.drawable.ic_profile_id_card),
                    label = "ID Number:",
                    value = patientId
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Clinic Section
                ProfileInfoItem(
                    painter = painterResource(id = R.drawable.ic_profile_hospital),
                    label = "醫療診所:",
                    value = vendorName
                )

                Spacer(modifier = Modifier.height(60.dp))

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TagGoGreen)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("登出", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.error!!, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(60.dp))
                // Version display at bottom as seen in screenshot
                Text(
                    text = "Tag&Go 應用程式版本: 2.0.35",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "您確定要登出應用程式？",
                    fontSize = 18.sp,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TagGoGreen)
                    ) {
                        Text("否", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TagGoGreen)
                    ) {
                        Text("是", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    painter: Painter,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .border(2.dp, TagGoGreen, CircleShape),
            shape = CircleShape,
            color = TagGoGreen
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp).padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color(0xFFAAAAAA), fontSize = 16.sp)
        Text(text = value, color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
