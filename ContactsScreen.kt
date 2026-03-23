package com.example.netspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToCall: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChats: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(2) }
    val tabs = listOf("Chats", "Calls", "Contacts", "Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NETSpace", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            when (index) {
                                0 -> onNavigateToChats()
                                3 -> onNavigateToSettings()
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                2 -> {
                    // Contacts tab
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(5) { index ->
                            ContactCard(
                                name = "Contact Name",
                                isOnline = index % 2 == 0,
                                onCardClick = onNavigateToProfile,
                                onCallClick = onNavigateToCall,
                                onVideoCallClick = onNavigateToCall  // FIX: video -> incall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                1 -> {
                    // Calls tab placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Calls history", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    name: String,
    isOnline: Boolean,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },  // FIX: click pe card -> profil
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color.Green else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "online" else "offline",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Source/Date placeholder", color = Color.Gray, fontSize = 12.sp)
                Row {
                    // FIX: Phone -> onCallClick (audio call)
                    IconButton(onClick = onCallClick) {
                        Icon(Icons.Default.Phone, contentDescription = "Call")
                    }
                    // FIX: Video -> onVideoCallClick (video call / incall)
                    IconButton(onClick = onVideoCallClick) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                }
            }
        }
    }
}