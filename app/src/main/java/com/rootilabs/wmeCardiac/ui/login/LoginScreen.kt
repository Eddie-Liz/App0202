package com.rootilabs.wmeCardiac.ui.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rootilabs.wmeCardiac.ui.theme.TagGoGreen

import androidx.compose.ui.res.stringResource
import com.rootilabs.wmeCardiac.R

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

    if (uiState.showScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { barcode ->
                viewModel.onBarcodeScanned(barcode)
            },
            onDismiss = {
                viewModel.onScannerDismissed()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF616161))
    ) {
        // Green toolbar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TagGoGreen)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.welcome),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Avatar
            Image(
                painter = painterResource(id = R.drawable.icon_patient),
                contentDescription = "User",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.login_description),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account ID
            OutlinedTextField(
                value = viewModel.institutionId,
                onValueChange = { viewModel.institutionId = it.trim() },
                placeholder = { Text(stringResource(id = R.string.account_id), color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TagGoGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ID Number
            OutlinedTextField(
                value = viewModel.patientId,
                onValueChange = { viewModel.patientId = it.trim() },
                placeholder = { Text(stringResource(id = R.string.id_number), color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TagGoGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Server Region Dropdown — premium style
            var serverDropdownExpanded by remember { mutableStateOf(false) }
            val arrowRotation by animateFloatAsState(
                targetValue = if (serverDropdownExpanded) 180f else 0f,
                label = "arrow"
            )

            ExposedDropdownMenuBox(
                expanded = serverDropdownExpanded,
                onExpandedChange = { if (!uiState.isLoading) serverDropdownExpanded = it }
            ) {
                // Trigger row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .shadow(2.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.selectedServer.label,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.select_server),
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(arrowRotation)
                    )
                }

                // Dropdown menu
                ExposedDropdownMenu(
                    expanded = serverDropdownExpanded,
                    onDismissRequest = { serverDropdownExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                ) {
                    ServerRegion.values().forEachIndexed { index, region ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = region.label,
                                        fontSize = 15.sp,
                                        color = if (region == viewModel.selectedServer)
                                            TagGoGreen else Color(0xFF212121),
                                        fontWeight = if (region == viewModel.selectedServer)
                                            FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (region == viewModel.selectedServer) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TagGoGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectedServer = region
                                serverDropdownExpanded = false
                            },
                            modifier = Modifier.background(
                                if (region == viewModel.selectedServer)
                                    TagGoGreen.copy(alpha = 0.06f) else Color.Transparent
                            )
                        )
                        if (index < ServerRegion.values().lastIndex) {
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Scanner Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // Open scanner logic
                        viewModel.onScanClicked()
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qrcode_icon),
                    contentDescription = "Scan QR/Barcode",
                    modifier = Modifier.size(120.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCDD2)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error,
                        color = Color(0xFFB71C1C),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
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
                Text(stringResource(id = R.string.login), fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
