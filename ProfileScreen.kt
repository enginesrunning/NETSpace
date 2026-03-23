package com.example.netspace

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onStartCall: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.Gray,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Contact Name", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(text = "Available", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Call & Video buttons
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OutlinedButton(
                    onClick = onStartCall,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call")
                }
                OutlinedButton(
                    onClick = onStartCall,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Description placeholder", fontSize = 14.sp)
                            Text("[Shared files placeholder]", color = Color.Gray, fontSize = 12.sp)
                            Text("[Shared media placeholder]", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Title placeholder", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("[Shared files placeholder]", color = Color.Gray, fontSize = 12.sp)
                    Text("Shared Media Label", color = Color.Gray, fontSize = 12.sp)
                    Text("Shared Files Label", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Open Chat button
            Button(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Open Chat", fontSize = 16.sp)
            }
        }
    }
}