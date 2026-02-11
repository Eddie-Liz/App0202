package com.example.app0202.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app0202.ui.theme.TagGoGreen

@Composable
fun TagFlowOverlay(viewModel: MainViewModel) {
    val uiState = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Green header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TagGoGreen)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val headerText = when (uiState.tagFlowStep) {
                        TagFlowStep.CONFIRMATION -> "新的標註"
                        else -> "您剛剛按了標註！"
                    }
                    Text(headerText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    if (uiState.tagFlowStep == TagFlowStep.CONFIRMATION) {
                        IconButton(
                            onClick = { viewModel.cancelTagFlow() },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Tag time
                Text(
                    text = "標註時間：${uiState.tagTimeFormatted}",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Divider(color = Color(0xFFE0E0E0))

                // Content based on step
                when (uiState.tagFlowStep) {
                    TagFlowStep.SYMPTOM_SELECTION -> SymptomSelectionContent(viewModel)
                    TagFlowStep.EXERCISE_SELECTION -> ExerciseSelectionContent(viewModel)
                    TagFlowStep.CONFIRMATION -> ConfirmationContent(viewModel)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SymptomSelectionContent(viewModel: MainViewModel) {
    val uiState = viewModel.uiState

    Text(
        text = "1. 您感覺到甚麼症狀嗎？",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        SYMPTOM_LIST.forEach { symptom ->
            val isSelected = uiState.selectedSymptoms.contains(symptom.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleSymptom(symptom.id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(symptom.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(symptom.name, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = TagGoGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Divider(color = Color(0xFFF0F0F0))
        }

        // Other symptom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🗣️", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("其他", fontSize = 16.sp)
        }
        OutlinedTextField(
            value = uiState.otherSymptom,
            onValueChange = { viewModel.setOtherSymptom(it) },
            placeholder = { Text("語音輸入症狀", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }

    // Bottom buttons
    TagFlowBottomButtons(
        leftText = "取消誤觸",
        rightText = "下一步",
        onLeft = { viewModel.cancelTagFlow() },
        onRight = { viewModel.goToExerciseSelection() }
    )
}

@Composable
private fun ColumnScope.ExerciseSelectionContent(viewModel: MainViewModel) {
    val uiState = viewModel.uiState

    Text(
        text = "2. 您剛剛的運動強度是？",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        EXERCISE_LIST.forEach { exercise ->
            val isSelected = uiState.selectedExercise == exercise.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectExercise(exercise.id) }
                    .background(if (isSelected) Color(0xFFF5F5F5) else Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(exercise.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(exercise.description, fontSize = 13.sp, color = Color.Gray)
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = TagGoGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Divider(color = Color(0xFFF0F0F0))
        }
    }

    TagFlowBottomButtons(
        leftText = "取消誤觸",
        rightText = "確定",
        rightEnabled = uiState.selectedExercise >= 0,
        onLeft = { viewModel.cancelTagFlow() },
        onRight = { viewModel.goToConfirmation() }
    )
}

@Composable
private fun ColumnScope.ConfirmationContent(viewModel: MainViewModel) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Symptoms summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.goBackToSymptoms() },
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("您感受到的症狀", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val selectedNames = SYMPTOM_LIST
                        .filter { uiState.selectedSymptoms.contains(it.id) }
                        .joinToString("  ") { "${it.icon} ${it.name}" }
                    if (selectedNames.isNotEmpty()) {
                        Text(selectedNames, fontSize = 14.sp, color = Color(0xFF616161))
                    }
                    if (uiState.otherSymptom.isNotBlank()) {
                        Text("其他: ${uiState.otherSymptom}", fontSize = 14.sp, color = Color(0xFF616161))
                    }
                    if (selectedNames.isEmpty() && uiState.otherSymptom.isBlank()) {
                        Text("無選擇", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Text("›", fontSize = 24.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Exercise summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.goBackToExercise() },
            color = Color(0xFFFAFAFA),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("當時的運動強度", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val exercise = EXERCISE_LIST.find { it.id == uiState.selectedExercise }
                    Text(
                        text = "${exercise?.icon ?: ""} ${exercise?.name ?: "未選擇"}",
                        fontSize = 14.sp,
                        color = Color(0xFF616161)
                    )
                }
                Text("›", fontSize = 24.sp, color = Color.Gray)
            }
        }
    }

    TagFlowBottomButtons(
        leftText = "取消",
        rightText = "確定",
        rightEnabled = true,
        onLeft = { viewModel.cancelTagFlow() },
        onRight = { viewModel.confirmTag() }
    )
}

@Composable
private fun TagFlowBottomButtons(
    leftText: String,
    rightText: String,
    rightEnabled: Boolean = true,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onLeft,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF616161))
        ) {
            Text(leftText, fontSize = 16.sp)
        }

        Button(
            onClick = onRight,
            modifier = Modifier.weight(1f).height(48.dp),
            enabled = rightEnabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TagGoGreen,
                disabledContainerColor = Color(0xFFBDBDBD)
            )
        ) {
            Text(rightText, fontSize = 16.sp, color = Color.White)
        }
    }
}
