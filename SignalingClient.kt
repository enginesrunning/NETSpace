package com.example.netspace.webrtc

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SignalingEvent {
    data class CallReceived(val callerId: String) : SignalingEvent()
    data class OfferReceived(val sdp: String, val senderId: String) : SignalingEvent()
    data class AnswerReceived(val sdp: String, val senderId: String) : SignalingEvent()
    data class IceCandidateReceived(val sdpMid: String, val sdpMLineIndex: Int, val candidate: String, val senderId: String) : SignalingEvent()
    data class CallEnded(val senderId: String) : SignalingEvent()
}

class SignalingClient(private val ipAddress: String, private val currentUserId: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _events = MutableSharedFlow<SignalingEvent>()
    val events: SharedFlow<SignalingEvent> = _events
    
    // Quick Coroutine scope for emitting
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        Log.d("SignalingClient", "Connecting to ws://$ipAddress:8080")
        val request = Request.Builder()
            .url("ws://$ipAddress:8080")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("SignalingClient", "Connected. Logging in as $currentUserId")
                val loginMsg = JSONObject().apply {
                    put("type", "login")
                    put("userId", currentUserId)
                }
                webSocket.send(loginMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.getString("type")
                    val senderId = json.optString("senderId", "")
                    
                    Log.d("SignalingClient", "Received signal: $type from $senderId")

                    when (type) {
                        "call" -> scope.launch { _events.emit(SignalingEvent.CallReceived(senderId)) }
                        "offer" -> scope.launch { _events.emit(SignalingEvent.OfferReceived(json.getString("sdp"), senderId)) }
                        "answer" -> scope.launch { _events.emit(SignalingEvent.AnswerReceived(json.getString("sdp"), senderId)) }
                        "ice_candidate" -> scope.launch {
                            _events.emit(SignalingEvent.IceCandidateReceived(
                                json.getString("id"),
                                json.getInt("label"),
                                json.getString("candidate"),
                                senderId
                            ))
                        }
                        "end_call" -> scope.launch { _events.emit(SignalingEvent.CallEnded(senderId)) }
                    }
                } catch (e: Exception) {
                    Log.e("SignalingClient", "Message parse error", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SignalingClient", "Connection failed", t)
            }
        })
    }

    fun initiateCall(targetId: String) {
        val msg = JSONObject().apply {
            put("type", "call")
            put("targetId", targetId)
        }
        webSocket?.send(msg.toString())
    }

    fun sendOffer(targetId: String, sdp: String) {
        val msg = JSONObject().apply {
            put("type", "offer")
            put("targetId", targetId)
            put("sdp", sdp)
        }
        webSocket?.send(msg.toString())
    }

    fun sendAnswer(targetId: String, sdp: String) {
        val msg = JSONObject().apply {
            put("type", "answer")
            put("targetId", targetId)
            put("sdp", sdp)
        }
        webSocket?.send(msg.toString())
    }

    fun sendIceCandidate(targetId: String, sdpMid: String, sdpMLineIndex: Int, candidate: String) {
        val msg = JSONObject().apply {
            put("type", "ice_candidate")
            put("targetId", targetId)
            put("id", sdpMid)
            put("label", sdpMLineIndex)
            put("candidate", candidate)
        }
        webSocket?.send(msg.toString())
    }

    fun endCall(targetId: String) {
        val msg = JSONObject().apply {
            put("type", "end_call")
            put("targetId", targetId)
        }
        webSocket?.send(msg.toString())
    }

    fun destroy() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}
