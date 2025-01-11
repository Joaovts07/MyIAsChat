package com.example.myiachats.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myiachats.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val question by viewModel.question.collectAsState() // Observa o fluxo de estado
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = question,
            onValueChange = { viewModel.onQuestionChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Digite sua pergunta") }
        )

        Button(
            onClick = { viewModel.sendQuestion() },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Enviar")
            }
        }

        Text(
            text = answer ?: "Resposta aparecerá aqui",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

