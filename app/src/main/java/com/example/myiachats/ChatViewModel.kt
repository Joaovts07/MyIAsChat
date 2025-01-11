package com.example.myiachats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question

    private val _answer = MutableStateFlow<String?>(null)
    val answer: StateFlow<String?> = _answer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }



    fun onQuestionChanged(newQuestion: String) {
        _question.value = newQuestion
    }

    fun sendQuestion() {
        val userQuestion = _question.value
        if (userQuestion.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response: ApiResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer SUA_CHAVE_API")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "model" to "gpt-3.5-turbo",
                            "messages" to listOf(mapOf("role" to "user", "content" to userQuestion))
                        )
                    )
                }.body()

                _answer.value = response.choices.firstOrNull()?.message?.content ?: "Sem resposta"
            } catch (e: Exception) {
                _answer.value = "Erro: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ApiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val role: String,
    val content: String
)

