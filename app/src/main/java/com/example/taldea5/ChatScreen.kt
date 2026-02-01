package com.example.taldea5



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taldea5.ui.theme.SushiRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    host: String,
    port: Int = 5555,
    userName: String?,
    onBack: () -> Unit
) {
    val client = remember(host, port) { TcpChatClient(host, port) }

    val connected by client.connected.collectAsState()
    val messages by client.messages.collectAsState()
    val error by client.error.collectAsState()

    var input by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        client.connect()
        onDispose { client.disconnect() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Txata",
                    color = SushiRed,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = SushiRed)
                }
            },
            actions = {
                Text(
                    text = if (connected) "Konektatuta" else "Konektatu gabe",
                    color = if (connected) SushiRed else Color.Gray,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { m ->
                ChatBubble(fromMe = m.fromMe, text = m.text)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Idatzi mezua…") }
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val prefix = userName?.let { "$it: " } ?: ""
                    client.send(prefix + input)
                    input = ""
                },
                enabled = connected && input.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = SushiRed)
            }
        }
    }
}

@Composable
private fun ChatBubble(fromMe: Boolean, text: String) {
    val bg = if (fromMe) SushiRed.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val align = if (fromMe) Alignment.End else Alignment.Start

    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 300.dp),

        ) {
            Text(text = text, color = Color.Black)
        }
    }
}
