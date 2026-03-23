package com.example.netspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.netspace.ui.theme.NETSpaceTheme

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
    NavHost(navController = navController, startDestination = "contacts") {
        composable("contacts") {
            ContactsScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToCall = { navController.navigate("incall") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToChats = { navController.navigate("chats") }
            )
        }
        composable("chat") {
            ChatScreen(onBack = { navController.popBackStack() })
        }
        composable("chats") {
            ChatsScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { navController.navigate("chat") }
            )
        }
        composable("incall") {
            InCallScreen(onEndCall = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(onBack = { navController.popBackStack() },
                onOpenChat = { navController.navigate("chat") },
                onStartCall = { navController.navigate("incall") }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}