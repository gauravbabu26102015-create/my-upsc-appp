package com.myguruu.app.features.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpscAgentScreen() {
    var messages by remember { 
        mutableStateOf(
            listOf(
                ChatMessage("Hello Future Officer! I'm your UPSC Agent. How can I help you today?", false),
                ChatMessage("Here's your daily motivation: 'Success is the sum of small efforts, repeated day in and day out.' Keep studying hard!", false)
            )
        )
    }
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("UPSC Agent") })
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask for advice or motivation...") },
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userText = inputText
                            messages = messages + ChatMessage(userText, true)
                            inputText = ""
                            
                            // Simple simulated responses
                            val response = generateAgentResponse(userText)
                            messages = messages + ChatMessage(response, false)
                        }
                    }
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = if (!message.isUser && message.text.contains("'")) FontStyle.Italic else FontStyle.Normal
            )
        }
    }
}

fun generateAgentResponse(input: String): String {
    val lowerInput = input.lowercase()
    return when {
        lowerInput.contains("motivation") -> "Remember why you started. Every page you read brings you closer to LBSNAA!"
        lowerInput.contains("advice") || lowerInput.contains("tip") -> "Focus on answer writing and consistent revision. Quality over quantity!"
        lowerInput.contains("tired") || lowerInput.contains("exhausted") -> "It's a marathon, not a sprint. Take a short 15-minute break and drink some water."
        else -> "I'm your AI assistant. I'll analyze that and get back to you with the best UPSC strategy. For now, keep focusing on your syllabus!"
    }
}
