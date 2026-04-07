package com.example.netspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onNavigateToChat: (Int) -> Unit,
    onNavigateToCall: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
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
                        items(AppState.contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                onCardClick = { onNavigateToProfile(contact.id) },
                                onCallClick = { onNavigateToCall(contact.id) },
                                onVideoCallClick = { onNavigateToCall(contact.id) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                1 -> {
                    // Calls tab 
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (AppState.callLog.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No recent calls", color = Color.Gray)
                                }
                            }
                        } else {
                            items(AppState.callLog) { call ->
                                CallLogItem(call)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallLogItem(call: CallRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (call.isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
            contentDescription = null,
            tint = if (call.isOutgoing) Color.Green else Color.Red
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = call.contactName, fontWeight = FontWeight.Bold)
            Text(text = "${call.time} • Duration: ${call.duration}", fontSize = 12.sp, color = Color.Gray)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}

@Composable
fun ContactCard(
    contact: Contact,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
                    Text(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (contact.isOnline) Color.Green else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (contact.isOnline) "online" else "offline",
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
                Text(contact.status, color = Color.Gray, fontSize = 12.sp)
                Row {
                    IconButton(onClick = onCallClick) {
                        Icon(Icons.Default.Phone, contentDescription = "Call")
                    }
                    IconButton(onClick = onVideoCallClick) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                }
            }
        }
    }
}