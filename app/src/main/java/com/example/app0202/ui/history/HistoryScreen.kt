package com.example.app0202.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app0202.data.local.EventTagDbEntity
import com.example.app0202.ui.main.EXERCISE_LIST
import com.example.app0202.ui.main.SYMPTOM_LIST
import com.example.app0202.ui.theme.TagGoGreen

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val tags = viewModel.tags

    Column(modifier = Modifier.fillMaxSize()) {
        // Green toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TagGoGreen)
                .padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "標註紀錄",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (tags.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("目前沒有標註紀錄", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEEEEEE)),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    HistoryTagCard(tag)
                }
            }
        }
    }
}

@Composable
fun HistoryTagCard(tag: EventTagDbEntity) {
    val exerciseName = EXERCISE_LIST.find { it.id == tag.exerciseIntensity }
    val symptomNames = SYMPTOM_LIST.filter { tag.eventType.contains(it.id) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = tag.tagLocalTime,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (symptomNames.isNotEmpty()) {
                Text("症狀:", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = symptomNames.joinToString("、") { "${it.icon} ${it.name}" },
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (!tag.others.isNullOrBlank()) {
                Text("其他: ${tag.others}", fontSize = 14.sp, color = Color(0xFF757575))
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (exerciseName != null) {
                Text("運動強度:", fontSize = 13.sp, color = Color.Gray)
                Text("${exerciseName.icon} ${exerciseName.name}", fontSize = 14.sp)
            }
        }
    }
}
