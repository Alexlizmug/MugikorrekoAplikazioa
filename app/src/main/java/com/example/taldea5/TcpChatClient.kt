package com.example.taldea5

import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

data class ChatMessage(
    val fromMe: Boolean,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis()
)

class TcpChatClient(
    private val host: String,
    private val port: Int,
    private val mesaId: Int?,
    private val mesaName: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var readJob: Job? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun connect() {
        if (_connected.value) return
        if (mesaId == null) {
            _error.value = "Mahaia identifikatu gabe dago."
            return
        }

        _error.value = null
        scope.launch {
            try {
                val s = Socket(host, port)
                socket = s
                reader = BufferedReader(InputStreamReader(s.getInputStream()))
                writer = BufferedWriter(OutputStreamWriter(s.getOutputStream()))
                sendRaw(buildRegisterMessage(mesaId, mesaName))
                _connected.value = true

                readJob?.cancel()
                readJob = scope.launch {
                    try {
                        while (isActive && socket?.isConnected == true) {
                            val line = reader?.readLine() ?: break
                            handleServerLine(line)
                        }
                    } catch (e: Exception) {
                        _error.value = "Chat irakurtze errorea: ${e.message}"
                    } finally {
                        disconnect()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ezin konektatu: ${e.message}"
                disconnect()
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try { readJob?.cancel() } catch (_: Exception) {}
            try { reader?.close() } catch (_: Exception) {}
            try { writer?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}

            readJob = null
            reader = null
            writer = null
            socket = null
            _connected.value = false
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isEmpty()) return
        val currentMesaId = mesaId ?: run {
            _error.value = "Mahaia identifikatu gabe dago."
            return
        }

        addMessage(fromMe = true, text = msg)

        scope.launch {
            try {
                if (writer == null) {
                    _error.value = "Ez dago konexiorik."
                    return@launch
                }

                sendRaw(buildMesaChatMessage(currentMesaId, mesaName, msg))
            } catch (e: Exception) {
                _error.value = "Bidaltze errorea: ${e.message}"
            }
        }
    }

    private fun handleServerLine(line: String) {
        val parts = line.split("|")
        if (parts.size < 2) return

        when (parts[0]) {
            "CHAT" -> {
                if (parts.size < 5) return
                if (parts[1] != "TPV") return

                val messageText = decode(parts[4])
                addMessage(fromMe = false, text = messageText)
            }
        }
    }

    private fun buildRegisterMessage(mesaId: Int, mesaName: String): String {
        return "REGISTER|MESA|$mesaId|${encode(mesaName)}"
    }

    private fun buildMesaChatMessage(mesaId: Int, mesaName: String, text: String): String {
        return "CHAT|MESA|$mesaId|${encode(mesaName)}|${encode(text)}"
    }

    private fun sendRaw(text: String) {
        writer?.apply {
            write(text)
            newLine()
            flush()
        } ?: run {
            _error.value = "Ez dago konexiorik."
        }
    }

    private fun encode(value: String): String {
        return Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun decode(value: String): String {
        return String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8)
    }

    private fun addMessage(fromMe: Boolean, text: String) {
        val current = _messages.value
        _messages.value = current + ChatMessage(fromMe = fromMe, text = text)
    }
}

