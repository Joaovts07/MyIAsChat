package com.example.myiachats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
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
        val apiKey = BuildConfig.API_KEY
        if (userQuestion.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "model" to "gpt-3.5-turbo",
                            "messages" to listOf(mapOf("role" to "user", "content" to userQuestion))
                        )
                    )
                }

                if (response.status == HttpStatusCode.OK) {
                    val apiResponse: ApiResponse = response.body()
                    if (apiResponse.choices != null && apiResponse.choices.isNotEmpty()) {
                        _answer.value = apiResponse.choices.first().message.content
                        println("Primeira mensagem: ${apiResponse.choices.first().message.content}")
                    } else {
                        println("Nenhuma mensagem recebida ou 'choices' é null.")
                        _answer.value = "Nenhuma resposta da API."
                    }
                } else {
                    println("Erro HTTP: ${response.status}")
                    _answer.value = "Erro HTTP: ${response.status}"
                }

            } catch (e: Exception) {
                _answer.value = "Erro: ${e.localizedMessage}"
                println("Erro ao fazer a requisição: ${e.localizedMessage}")
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

