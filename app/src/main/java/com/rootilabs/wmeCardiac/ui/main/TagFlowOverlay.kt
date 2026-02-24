package com.rootilabs.wmeCardiac.ui.main

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rootilabs.wmeCardiac.R
import com.rootilabs.wmeCardiac.ui.theme.TagGoGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFlowOverlay(
    viewModel: MainViewModel,
    onStartVoiceInput: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.cancelTagFlow() },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {}, // Hide drag handle
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            // Unified Turquoise Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TagGoGreen) // Consistent Turquoise
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val headerText = "您剛剛按了標註！"
                Text(headerText, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (uiState.tagFlowStep == TagFlowStep.CONFIRMATION) {
                    IconButton(
                        onClick = { viewModel.cancelTagFlow() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Tag time section with dividers
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "標註時間: ${uiState.tagTimeFormatted}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF424242)
                )
            }
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Content based on step
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.tagFlowStep) {
                    TagFlowStep.SYMPTOM_SELECTION -> SymptomSelectionContent(viewModel, onStartVoiceInput)
                    TagFlowStep.EXERCISE_SELECTION -> ExerciseSelectionContent(viewModel)
                    TagFlowStep.CONFIRMATION -> ConfirmationContent(viewModel)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SymptomSelectionContent(
    viewModel: MainViewModel,
    onStartVoiceInput: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "1. 您感覺到甚麼症狀嗎？",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF424242)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SYMPTOM_LIST.forEachIndexed { index, symptom ->
                    val isSelected = uiState.selectedSymptoms.contains(symptom.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSymptom(symptom.id) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tint = Color(0xFF757575)
                        when (val icon = symptom.icon) {
                            is IconSource.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(64.dp)
                            )
                            is IconSource.Resource -> Icon(
                                painter = painterResource(id = icon.resId),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(64.dp)
                            )
                            is IconSource.Text -> Text(text = icon.text, fontSize = 32.sp, modifier = Modifier.size(64.dp).wrapContentSize(Alignment.Center))
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = symptom.name,
                            fontSize = 18.sp,
                            color = Color(0xFF424242),
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = TagGoGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    if (index < SYMPTOM_LIST.lastIndex) {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Other symptom
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_symptom_other),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("其他(請輸入其他症狀)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
            }
            OutlinedTextField(
                value = uiState.otherSymptom,
                onValueChange = { viewModel.setOtherSymptom(it) },
                placeholder = { Text("點擊右側麥克風語音輸入...", color = Color.Gray, fontSize = 14.sp) },
                trailingIcon = {
                    IconButton(onClick = onStartVoiceInput) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = TagGoGreen
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom buttons
        TagFlowBottomButtons(
            leftText = "取消誤觸",
            rightText = "下一步",
            rightEnabled = uiState.selectedSymptoms.isNotEmpty() || uiState.otherSymptom.isNotBlank(),
            onLeft = { viewModel.cancelTagFlow() },
            onRight = { viewModel.goToExerciseSelection() }
        )
    }
}

@Composable
private fun BoxScope.ExerciseSelectionContent(viewModel: MainViewModel) {
    val uiState = viewModel.uiState

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "2. 您剛剛的運動強度是？",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF424242)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            EXERCISE_LIST.forEach { exercise ->
                val isSelected = uiState.selectedExercise == exercise.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectExercise(exercise.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFE0F2F1) else Color.White,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, TagGoGreen) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tint = Color(0xFF757575)
                        when (val icon = exercise.icon) {
                            is IconSource.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(64.dp)
                            )
                            is IconSource.Resource -> Icon(
                                painter = painterResource(id = icon.resId),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(64.dp)
                            )
                            is IconSource.Text -> Text(text = icon.text, fontSize = 32.sp, modifier = Modifier.size(64.dp).wrapContentSize(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                            Text(exercise.description, fontSize = 13.sp, color = Color(0xFF616161))
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
                }
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
}

@Composable
private fun BoxScope.ConfirmationContent(viewModel: MainViewModel) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // 1. Symptoms Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.goBackToSymptoms() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1. 您感受到的症狀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF424242),
                    modifier = Modifier.weight(1f)
                )
                Text("›", fontSize = 40.sp, color = Color(0xFFBDBDBD))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val selectedSymptoms = SYMPTOM_LIST.filter { uiState.selectedSymptoms.contains(it.id) }
                if (selectedSymptoms.isEmpty() && uiState.otherSymptom.isBlank()) {
                    Text("尚未選擇任何症狀", color = Color.Gray, fontSize = 18.sp)
                } else {
                    selectedSymptoms.forEach { symptom ->
                        ConfirmationIconRow(icon = symptom.icon, text = symptom.name)
                    }
                    if (uiState.otherSymptom.isNotBlank()) {
                        ConfirmationIconRow(
                            icon = IconSource.Resource(R.drawable.ic_symptom_other),
                            text = "其他: ${uiState.otherSymptom}"
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.goBackToExercise() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. 當下運動強度",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF424242),
                    modifier = Modifier.weight(1f)
                )
                Text("›", fontSize = 40.sp, color = Color(0xFFBDBDBD))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                val exercise = EXERCISE_LIST.find { it.id == uiState.selectedExercise }
                if (exercise == null) {
                    Text("尚未選擇運動強度", color = Color.Gray, fontSize = 18.sp)
                } else {
                    ConfirmationIconRow(icon = exercise.icon, text = exercise.name)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(40.dp))

        TagFlowBottomButtons(
            leftText = "修改",
            rightText = "傳送",
            rightEnabled = true,
            onLeft = { viewModel.goBackToSymptoms() },
            onRight = { viewModel.confirmTag() }
        )
    }
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
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF757575),
                contentColor = Color.White
            ),
            border = null
        ) {
            Text(leftText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onRight,
            modifier = Modifier.weight(1f).height(56.dp),
            enabled = rightEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TagGoGreen,
                disabledContainerColor = Color(0xFFBDBDBD)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(rightText, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConfirmationIconRow(icon: IconSource, text: String) {
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
