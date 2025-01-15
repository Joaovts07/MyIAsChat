package com.example.myiachats.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myiachats.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val question by viewModel.question.collectAsState()
    val chatGptAnswer by viewModel.chatGptAnswer.collectAsState()
    val geminiAnswer by viewModel.geminiAnswer.collectAsState()
    val bingAnswer by viewModel.bingAnswer.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Pergunte ao ChatGPT",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = question,
            onValueChange = { viewModel.onQuestionChanged(it) },
            label = { Text("Digite sua pergunta") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Button(
            onClick = { viewModel.sendBothQuestionsInParallel() },
            enabled = !isLoading && question.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text("Enviar")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ResponseItem(titulo = "ChatGPT", resposta = chatGptAnswer)
            }
            item {
                ResponseItem(titulo = "Gemini", resposta = geminiAnswer)
            }
            item {
                ResponseItem(titulo = "Bing", resposta = bingAnswer)
            }
        }
    }
}

@Composable
fun ResponseItem(titulo: String, resposta: String?) {
    if (resposta != null) {
        Text(
            text = "Resposta de $titulo:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = resposta,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

