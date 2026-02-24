package com.rootilabs.wmeCardiac.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rootilabs.wmeCardiac.ui.theme.TagGoGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF616161))
    ) {
        // Green toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TagGoGreen)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "歡迎",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBDBDBD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "請填寫資料並登入",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account ID
            OutlinedTextField(
                value = viewModel.institutionId,
                onValueChange = { viewModel.institutionId = it.trim() },
                placeholder = { Text("Account ID", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TagGoGreen
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ID Number
            OutlinedTextField(
                value = viewModel.patientId,
                onValueChange = { viewModel.patientId = it.trim() },
                placeholder = { Text("ID Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TagGoGreen
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Region (static for now)
            OutlinedTextField(
                value = "Asia-Pacific",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCDD2)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = uiState.error,
                            color = Color(0xFFB71C1C),
                            fontSize = 14.sp
                        )
                        
                        // Suggest Force Login if it's a conflict error
                        if (uiState.error.contains("此病患已在其他裝置登入")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.forceLogin() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB71C1C)
                                ),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("強制登入 (解除其他裝置)", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Loading status
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TagGoGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uiState.statusMessage, color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Login button
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TagGoGreen,
                    disabledContainerColor = TagGoGreen.copy(alpha = 0.5f)
                )
            ) {
                Text("登入", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
