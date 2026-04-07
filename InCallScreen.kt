package com.example.netspace

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.netspace.webrtc.SignalingEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun InCallScreen(
    contactId: Int,
    onEndCall: () -> Unit,
    onOpenChat: () -> Unit
) {
    val contact = AppState.contacts.find { it.id == contactId } ?: return
    val context = LocalContext.current
    val webRTCManager = AppState.webRTCManager
    val signalingClient = AppState.signalingClient

    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaker by remember { mutableStateOf(false) }
    var isVideo by remember { mutableStateOf(true) }

    // Permission handling
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    // View Renderers
    val localRenderer = remember { SurfaceViewRenderer(context) }
    val remoteRenderer = remember { SurfaceViewRenderer(context) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            webRTCManager?.let { manager ->
                manager.startLocalMedia()
                
                // Initialize renderers
                localRenderer.init(manager.getEglBaseContext(), null)
                localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                localRenderer.setMirror(true)
                
                remoteRenderer.init(manager.getEglBaseContext(), null)
                remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

                manager.localVideoSink = localRenderer
                manager.remoteVideoSink = remoteRenderer

                // If we are the ones initiating the call
                // (In a real app we'd check if we are 'offering' or 'answering')
                // For this MVP simplicity: we initiate if we navigated here via 'Call' button
                manager.initiateCall(contact.name)
            }
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // Timer logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            callDurationSeconds++
        }
    }

    // Handle incoming WebRTC signals
    LaunchedEffect(signalingClient) {
        signalingClient?.events?.collectLatest { event ->
            when (event) {
                is SignalingEvent.OfferReceived -> {
                    webRTCManager?.receiveOffer(event.senderId, event.sdp)
                }
                is SignalingEvent.AnswerReceived -> {
                    webRTCManager?.receiveAnswer(event.sdp)
                }
                is SignalingEvent.IceCandidateReceived -> {
                    webRTCManager?.receiveIceCandidate(event.sdpMid, event.sdpMLineIndex, event.candidate)
                }
                is SignalingEvent.CallEnded -> {
                    onEndCall()
                }
                else -> {}
            }
        }
    }

    val formattedTime = String.format("%02d:%02d", callDurationSeconds / 60, callDurationSeconds % 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("In-Call - ${contact.name}") },
                navigationIcon = {
                    IconButton(onClick = {
                        webRTCManager?.endCall()
                        AppState.addCallRecord(contactId, formattedTime, isOutgoing = true)
                        onEndCall()
                    }) { 
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back") 
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Remote Video (Background)
            AndroidView(
                factory = { remoteRenderer },
                modifier = Modifier.fillMaxSize()
            )

            // Local Video (Small overlay)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { localRenderer },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // UI Controls overlay
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section: Timer
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = formattedTime, 
                            fontSize = 18.sp, 
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Section: Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallControlButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, 
                            label = "Mute",
                            isActive = isMuted,
                            onClick = { 
                                isMuted = !isMuted 
                                webRTCManager?.setMute(isMuted)
                            }
                        )
                        CallControlButton(
                            icon = if (isVideo) Icons.Default.Videocam else Icons.Default.VideocamOff, 
                            label = "Video",
                            isActive = isVideo,
                            onClick = { 
                                isVideo = !isVideo
                                webRTCManager?.setVideoEnabled(isVideo)
                            }
                        )
                        CallControlButton(
                            icon = Icons.Default.VolumeUp, 
                            label = "Speaker",
                            isActive = isSpeaker,
                            onClick = { isSpeaker = !isSpeaker }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    CallControlButton(icon = Icons.Default.Chat, label = "Chat", isActive = false, onClick = onOpenChat)
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            webRTCManager?.endCall()
                            AppState.addCallRecord(contactId, formattedTime, isOutgoing = true)
                            onEndCall()
                        },
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

    // Cleanup when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            // manager?.dispose() is handled by endCall or activity lifecycle usually
            // but we ensure sinks are cleared
            webRTCManager?.localVideoSink = null
            webRTCManager?.remoteVideoSink = null
            localRenderer.release()
            remoteRenderer.release()
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector, 
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Black.copy(alpha = 0.6f), 
                    RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                icon, 
                contentDescription = label, 
                modifier = Modifier.size(30.dp),
                tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color.White)
    }
}