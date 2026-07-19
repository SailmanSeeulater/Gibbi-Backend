package com.ltslr.gibbi.config

import com.ltslr.gibbi.messaging.GibbiWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val gibbiWebSocketHandler: GibbiWebSocketHandler,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(gibbiWebSocketHandler, "/realtime")
            .setAllowedOrigins("*")
    }
}
