package com.example.myiachats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.gson.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question

    private val _chatGptAnswer = MutableStateFlow<String?>(null)
    val chatGptAnswer: StateFlow<String?> = _chatGptAnswer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _geminiAnswer = MutableStateFlow<String?>(null)
    val geminiAnswer: StateFlow<String?> = _geminiAnswer

    private val _bingAnswer = MutableStateFlow<String?>(null)
    val bingAnswer: StateFlow<String?> = _bingAnswer

    private val _deepSeekAnswer = MutableStateFlow<String?>(null)
    val deepSeekAnswer: StateFlow<String?> = _deepSeekAnswer

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
                callChatGptApi()
                callGeminiApi()
                callBingApi()
                callDeepSeekApi()
            } catch (e: Exception) {
                _chatGptAnswer.value = "Erro ao chamar ChatGPT: ${e.localizedMessage}"
                _geminiAnswer.value = "Erro ao chamar Gemini: ${e.localizedMessage}"
                _bingAnswer.value = "Erro ao chamar Bing: ${e.localizedMessage}"
                _deepSeekAnswer.value = "Erro ao chamar DeepSeek: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun callChatGptApi() {
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.API_KEY_CHATGPT
                val response: ApiResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    contentType(Json)
                    setBody(
                        mapOf(
                            "model" to "gpt-3.5-turbo",
                            "messages" to listOf(
                                mapOf(
                                    "role" to "user",
                                    "content" to _chatGptAnswer.value
                                )
                            )
                        )
                    )
                }.body()
                val choice = response.choices.firstOrNull()
                if (choice != null) {
                    _chatGptAnswer.value = choice.message.content
                } else {
                    _chatGptAnswer.value = "Nenhuma resposta recebida. Verifique os dados enviados."
                    println("Resposta da API: $response") // Log para depuração
                }
            } catch (e: Exception) {
                _chatGptAnswer.value = "Erro ao chamar a API: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    private suspend fun callGeminiApi() {
        try {
            val apiKey = BuildConfig.API_KEY_GEMINI
            val model = GenerativeModel(
                modelName = "gemini-1.5-pro",
                apiKey = apiKey
            )
            val message = _question.value.let { model.generateContent(it) }
            _geminiAnswer.value = message.text
        } catch (e: Exception) {
            "Erro ao chamar a outra API: ${e.localizedMessage}"
        }
    }

    private suspend fun callBingApi() {
        try {
            val apiKey = BuildConfig.API_KEY_BING
            val response: HttpResponse = httpClient.post("https://<seu-endpoint>.openai.azure.com/openai/deployments/<seu-deployment>/chat/completions?api-version=2023-05-15") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                }
                contentType(Json)
                setBody(
                    mapOf(
                        "messages" to listOf(
                            mapOf("role" to "user", "content" to _question.value)
                        )
                    )
                )
            }

            val jsonResponse = response.bodyAsText()
            println("Resposta do Azure OpenAI: $jsonResponse")

            val apiResponse = kotlinx.serialization.json.Json.decodeFromString<ApiResponse>(jsonResponse)
            val choice = apiResponse.choices.firstOrNull()
            if (choice != null) {
                _bingAnswer.value = choice.message.content
            } else {
                _bingAnswer.value = "Nenhuma resposta recebida do Azure OpenAI."
            }
        } catch (e: Exception) {
            _bingAnswer.value = "Erro ao chamar o Azure OpenAI: ${e.localizedMessage}"
            e.printStackTrace()
        }
    }

    private suspend fun callDeepSeekApi() {
        try {
            val apiKey = BuildConfig.API_KEY_DEEPSEEK
            val response: HttpResponse = httpClient.post("https://api.deepseek.com/v1/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                }
                contentType(Json)
                setBody(
                    mapOf(
                        "model" to "deepseek-1.0", // Substitua pelo modelo correto
                        "messages" to listOf(
                            mapOf("role" to "user", "content" to _question.value)
                        )
                    )
                )
            }

            val jsonResponse = response.bodyAsText()
            println("Resposta do DeepSeek: $jsonResponse")

            val apiResponse = kotlinx.serialization.json.Json.decodeFromString<ApiResponse>(jsonResponse)
            val choice = apiResponse.choices.firstOrNull()
            if (choice != null) {
                _deepSeekAnswer.value = choice.message.content
            } else {
                _deepSeekAnswer.value = "Nenhuma resposta recebida do DeepSeek."
            }
        } catch (e: Exception) {
            _deepSeekAnswer.value = "Erro ao chamar o DeepSeek: ${e.localizedMessage}"
            e.printStackTrace()
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