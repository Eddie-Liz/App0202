package com.example.app0202.ui.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app0202.data.local.EventTagDbEntity
import com.example.app0202.ui.main.EXERCISE_LIST
import com.example.app0202.ui.main.SYMPTOM_LIST
import com.example.app0202.ui.main.IconSource
import androidx.compose.ui.res.painterResource
import com.example.app0202.ui.theme.TagGoGreen

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val tags = viewModel.tags
    val isSyncing = viewModel.isSyncing

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Lime Green Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TagGoGreen)
                .statusBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "標記紀錄",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Subtitle section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0))
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "您的標記",
                color = Color(0xFF616161),
                fontSize = 18.sp
            )
        }

        // Tag List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(tags) { tag ->
                HistoryRow(tag)
                Divider(color = Color(0xFFE0E0E0))
            }
        }

        // Footer Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { viewModel.retryUnsyncedTags() },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D3D3), // Light grey
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "重新上傳",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRow(tag: EventTagDbEntity) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    
    val exercise = EXERCISE_LIST.find { it.id == tag.exerciseIntensity }
    val symptoms = SYMPTOM_LIST.filter { tag.eventType.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.tagLocalTime,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "⌄",
                fontSize = 34.sp,
                color = Color(0xFF757575),
                modifier = Modifier.scale(scaleX = 1.2f, scaleY = 1f).rotate(rotation)
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                // 1. Symptoms Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. 您感受到的症狀",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (symptoms.isEmpty() && tag.others.isNullOrBlank()) {
                            Text("無記錄症狀", color = Color.Gray, fontSize = 18.sp)
                        } else {
                            symptoms.forEach { symptom ->
                                IconDetailRow(icon = symptom.icon, text = symptom.name)
                            }
                            if (!tag.others.isNullOrBlank()) {
                                IconDetailRow(
                                    icon = IconSource.Resource(com.example.app0202.R.drawable.ic_symptom_other),
                                    text = "其他: ${tag.others}"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // 2. Exercise Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. 當下運動強度",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        if (exercise != null) {
                            IconDetailRow(icon = exercise.icon, text = exercise.name)
                        } else {
                            Text("無記錄強度", color = Color.Gray, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconDetailRow(icon: IconSource, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is IconSource.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(56.dp)
                )
                is IconSource.Resource -> Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(56.dp)
                )
                is IconSource.Text -> Text(
                    text = icon.text,
                    fontSize = 32.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 32.sp,
            color = Color(0xFF424242),
            fontWeight = FontWeight.Bold
        )
    }
}
