package com.example.netspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.netspace.ui.theme.NETSpaceTheme
import com.example.netspace.webrtc.SignalingClient
import com.example.netspace.webrtc.SignalingEvent
import com.example.netspace.webrtc.WebRTCManager
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NETSpaceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NETSpaceMainApp()
                }
            }
        }
    }
}

@Composable
fun NETSpaceMainApp() {
    val navController = rememberNavController()
    
    // Choose start destination based on Auth state
    val startDestination = if (AppState.isLoggedIn) "contacts" else "auth"
    val context = LocalContext.current

    // Initialize signaling and webrtc when logged in
    val setupManagers = {
        if (AppState.signalingClient == null) {
            // Use 10.0.2.2 for emulator, or replace with your PC's actual local IP for physical devices
            val signalingParams = SignalingClient("10.0.2.2", AppState.currentUsername)
            AppState.signalingClient = signalingParams
            AppState.webRTCManager = WebRTCManager(context, signalingParams)
            signalingParams.start()
        }
    }

    if (AppState.isLoggedIn) {
        setupManagers()
    }

    // Global listener for incoming calls
    LaunchedEffect(AppState.signalingClient) {
        AppState.signalingClient?.events?.collectLatest { event ->
            if (event is SignalingEvent.CallReceived) {
                // Find contact by name/id (for now we assume usernames match contact names for simplicity)
                val caller = AppState.contacts.find { it.name == event.callerId }
                val callerId = caller?.id ?: 1 // Fallback
                navController.navigate("incall/$callerId")
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                setupManagers()
                navController.navigate("contacts") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        composable("contacts") {
            ContactsScreen(
                onNavigateToChat = { contactId -> navController.navigate("chat/$contactId") },
                onNavigateToCall = { contactId -> navController.navigate("incall/$contactId") },
                onNavigateToProfile = { contactId -> navController.navigate("profile/$contactId") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToChats = { navController.navigate("chats") }
            )
        }
        
        composable(
            route = "chat/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: -1
            ChatScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() },
                onStartCall = { navController.navigate("incall/$contactId") }
            )
        }
        
        composable("chats") {
            ChatsScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { contactId -> navController.navigate("chat/$contactId") },
                onNavigateToContacts = {
                    navController.navigate("contacts") {
                        popUpTo("contacts") { inclusive = true }
                    }
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable(
            route = "incall/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: -1
            InCallScreen(
                contactId = contactId,
                onEndCall = { navController.popBackStack() },
                onOpenChat = { navController.navigate("chat/$contactId") { popUpTo("contacts") } }
            )
        }
        
        composable(
            route = "profile/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: -1
            ProfileScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() },
                onOpenChat = { navController.navigate("chat/$contactId") },
                onStartCall = { navController.navigate("incall/$contactId") }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    AppState.logout()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true } // Clear all history
                    }
                },
                onNavigateToContacts = {
                    navController.navigate("contacts") {
                        popUpTo("contacts") { inclusive = true }
                    }
                },
                onNavigateToChats = { navController.navigate("chats") }
            )
        }
    }
}