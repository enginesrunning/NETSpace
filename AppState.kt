package com.example.netspace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.netspace.webrtc.WebRTCManager
import com.example.netspace.webrtc.SignalingClient

// ── Data Models ────────────────────────────────────────────────────────────────

data class Contact(
    val id: Int,
    val name: String,
    val status: String,
    val isOnline: Boolean
)

data class Message(
    val text: String,
    val isMe: Boolean,
    val time: String
)

data class CallRecord(
    val contactId: Int,
    val contactName: String,
    val duration: String,  // e.g. "01:45"
    val isOutgoing: Boolean,
    val time: String
)

data class UserAccount(
    val username: String,
    val password: String
)

// ── Shared Application State ───────────────────────────────────────────────────

object AppState {

    // Auth
    var isLoggedIn by mutableStateOf(false)
    var currentUsername by mutableStateOf("")

    // WebRTC & Signaling
    var signalingClient: SignalingClient? = null
    var webRTCManager: WebRTCManager? = null

    val accounts = mutableStateListOf<UserAccount>()

    // Pre-seeded contacts
    val contacts = mutableStateListOf(
        Contact(1, "Alice Johnson",  "Hey there! 👋",            isOnline = true),
        Contact(2, "Bob Smith",      "Available",                 isOnline = false),
        Contact(3, "Carol White",    "In a meeting",              isOnline = true),
        Contact(4, "David Brown",    "Busy",                      isOnline = false),
        Contact(5, "Emma Davis",     "At the gym 💪",             isOnline = true)
    )

    // Messages keyed by contactId
    private val _messages = mutableStateMapOf<Int, SnapshotStateList<Message>>()

    // Call log
    val callLog = mutableStateListOf<CallRecord>()

    init {
        _messages[1] = mutableStateListOf(
            Message("Hey! How are you?",              isMe = false, time = "10:30"),
            Message("I'm good, thanks! Working on something fun.", isMe = true, time = "10:31"),
            Message("Nice! Tell me more 😊",          isMe = false, time = "10:32")
        )
        _messages[2] = mutableStateListOf(
            Message("Are you free tonight?",          isMe = false, time = "09:15"),
            Message("Yes, what's the plan?",          isMe = true,  time = "09:20")
        )
        _messages[3] = mutableStateListOf(
            Message("Meeting moved to 4 pm",          isMe = false, time = "08:00")
        )
    }

    fun getMessages(contactId: Int): SnapshotStateList<Message> {
        return _messages.getOrPut(contactId) { mutableStateListOf() }
    }

    fun lastMessage(contactId: Int): Message? = _messages[contactId]?.lastOrNull()

    fun sendMessage(contactId: Int, text: String) {
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        getMessages(contactId).add(Message(text.trim(), isMe = true, time = now))
    }

    fun addCallRecord(contactId: Int, duration: String, isOutgoing: Boolean) {
        val contact = contacts.find { it.id == contactId } ?: return
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        callLog.add(0, CallRecord(contactId, contact.name, duration, isOutgoing, now))
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun login(username: String, password: String): Boolean {
        val ok = accounts.any { it.username == username && it.password == password }
        if (ok) { isLoggedIn = true; currentUsername = username }
        return ok
    }

    fun register(username: String, password: String): Boolean {
        if (accounts.any { it.username == username }) return false
        accounts.add(UserAccount(username, password))
        isLoggedIn = true
        currentUsername = username
        return true
    }

    fun logout() {
        isLoggedIn = false
        currentUsername = ""
        signalingClient?.destroy()
        webRTCManager?.dispose()
        signalingClient = null
        webRTCManager = null
    }
}
