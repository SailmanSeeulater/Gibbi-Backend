package com.ltslr.gibbi.messaging

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class SessionRegistry {
    private val byUsername = ConcurrentHashMap<String, WebSocketSession>()

    fun register(username: String, session: WebSocketSession) {
        byUsername[username] = session
    }

    fun unregister(username: String) {
        byUsername.remove(username)
    }

    fun get(username: String): WebSocketSession? =
        byUsername[username]?.takeIf { it.isOpen }

    fun isOnline(username: String): Boolean = get(username) != null
}
