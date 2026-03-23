package com.example.netspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    IconButton(onClick = { /* settings icon */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                // User profile row
                item {
                    SettingsProfileRow(
                        name = "User Name",
                        subtitle = "Edit Profile"
                    )
                    HorizontalDivider()
                }

                // Settings items
                item {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Account",
                        subtitle = "Security, number change",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy",
                        subtitle = "Status, last seen visibility",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Chat,
                        title = "Chats",
                        subtitle = "Themes, wallpapers, history",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Sounds, message tones",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Storage and Data",
                        subtitle = "Network usage, media auto-down",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help",
                        subtitle = "Help Center, contact us",
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About NETSpace",
                        subtitle = "Version 1.2 Â· Source/Date placeholder",
                        onClick = {}
                    )
                }
            }

            // Log Out button
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = { /* log out */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Log Out", fontSize = 16.sp, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SettingsProfileRow(name: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}