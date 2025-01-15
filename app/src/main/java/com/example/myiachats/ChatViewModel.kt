package com.example.myiachats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.async
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

    private val _otherApiResponse = MutableStateFlow("")
    val otherApiResponse: StateFlow<String> = _otherApiResponse

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    fun onQuestionChanged(newQuestion: String) {
        _question.value = newQuestion
    }

    fun sendBothQuestionsInParallel() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val chatGptResponseDeferred = async { callChatGptApi() }
                val otherApiResponseDeferred = async { callOtherApi() }

                val chatGptResponse = chatGptResponseDeferred.await()
                val otherApiResponse = otherApiResponseDeferred.await()

                _answer.value = chatGptResponse
                _otherApiResponse.value = otherApiResponse
            } catch (e: Exception) {
                _answer.value = "Erro ao chamar APIs: ${e.localizedMessage}"
                _otherApiResponse.value = "Erro ao chamar APIs: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callChatGptApi(): String {
        return try {
            val apiKey = BuildConfig.API_KEY_CHATGPT
            val response: ApiResponse =
                httpClient.post("https://api.openai.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "model" to "gpt-3.5-turbo",
                            "messages" to listOf(
                                mapOf(
                                    "role" to "user",
                                    "content" to _question.value
                                )
                            )
                        )
                    )
                }.body()

            response.choices.firstOrNull()?.message?.content
                ?: "Nenhuma resposta recebida do ChatGPT."
        } catch (e: Exception) {
            "Erro ao chamar ChatGPT: ${e.localizedMessage}"
        }
    }

    private suspend fun callOtherApi(): String {
        return try {
            val apiKey = BuildConfig.API_KEY_GEMINI
            val model = GenerativeModel(
                modelName = "gemini-1.5-pro",
                apiKey = apiKey
            )
            val message = model.generateContent(_question.value)
            message.text
            ?: "Nenhuma resposta recebida da outra API."
        } catch (e: Exception) {
            "Erro ao chamar a outra API: ${e.localizedMessage}"
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

}