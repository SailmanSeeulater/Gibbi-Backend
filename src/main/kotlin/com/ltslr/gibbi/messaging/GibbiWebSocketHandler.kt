package com.ltslr.gibbi.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.ltslr.gibbi.auth.JwtService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class GibbiWebSocketHandler(
    private val jwtService: JwtService,
    private val sessionRegistry: SessionRegistry,
) : TextWebSocketHandler() {

    private val objectMapper = ObjectMapper()
    
    // Session that have connected by not yet authenticated
    private val pending = ConcurrentHashMap.newKeySet<String>()

    // Session -> Authenticated usernme, so we know who to unregister on close
    private val sessionUser = ConcurrentHashMap<String, String>()
    private val authTimeout = Executors.newSingleThreadScheduledExecutor()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        pending.add(session.id)
        // If no valid auth frame arrvies in 5s, close the socket
        authTimeout.schedule({
            if (pending.remove(session.id)) {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("auth timeout"))
            }
        }, 5, TimeUnit.SECONDS)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        if (pending.contains(session.id)) {
            authenticate(session, message)
            return
        }

        // Authentication frames (pings, etc.) later lmao
    }

    private fun authenticate(session: WebSocketSession, message: TextMessage) {
        val frame = try {
            objectMapper.readTree(message.payload)
        } catch (e: Exception) {
            session.close(CloseStatus.BAD_DATA.withReason("malform auth"))
            return
        }

        val type = frame.get("type")?.asText()
        val token = frame.get("token")?.asText()
        if (type != "auth" || token.isNullOrBlank() || !jwtService.isValid(token)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("auth failed"))
            return
        }

        val username = jwtService.extractUsername(token)
        pending.remove(session.id)
        sessionUser[session.id] = username
        sessionRegistry.register(username, session)
        session.sendMessage(TextMessage("""{"type":"auth_ok"}"""))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        pending.remove(session.id)
        sessionUser.remove(session.id)?.let { sessionRegistry.unregister(it) }
    }

    // Attempt live delivery. Return true only if the envelope was actually written to a socket
    fun deliver(toUsername: String, payload: String): Boolean {
        val session = sessionRegistry.get(toUsername) ?: return false
        return try {
            synchronized(session) {
                session.sendMessage(TextMessage(payload))
            }
            true
        } catch (e: Exception) {
            sessionRegistry.unregister(toUsername)
            false
        }
    }
}
