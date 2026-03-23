package com.example.netspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { /* Send message */ }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { ChatBubble(text = "Hello there!", isMe = false) }
            item { ChatBubble(text = "Hi! How are you?", isMe = true) }
            item { ChatBubble(text = "I'm doing well, working on a new Android app.", isMe = false) }
        }
    }
}

@Composable
fun ChatBubble(text: String, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 250.dp)
        ) {
            Text(text = text, modifier = Modifier.padding(12.dp))
        }
    }
}