package com.example.netspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun InCallScreen(onEndCall: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("In-Call") },
                navigationIcon = {
                    IconButton(onClick = onEndCall) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Name and Timer
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 32.dp)) {
                Text(text = "Contact Name", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "00:15", fontSize = 18.sp, color = Color.Gray)
            }

            // Middle Section: Large Avatar
            Box(
                modifier = Modifier.size(150.dp).clip(CircleShape).border(2.dp, Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray, modifier = Modifier.size(80.dp))
            }

            // Bottom Section: Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(icon = Icons.Default.MicOff, label = "Mute")
                    CallControlButton(icon = Icons.Default.Videocam, label = "Camera")
                    CallControlButton(icon = Icons.Default.VolumeUp, label = "Speaker")
                }

                Spacer(modifier = Modifier.height(24.dp))
                CallControlButton(icon = Icons.Default.Chat, label = "Chat")
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Call", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun CallControlButton(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp)
    }
}