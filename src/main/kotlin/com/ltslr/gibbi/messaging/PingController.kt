package com.ltslr.gibbi.messaging

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper


data class PingRequest(
    @field:NotBlank val to: String,
)

@RestController
@RequestMapping("/ping")
class PingController(
    private val webSocketHandler: GibbiWebSocketHandler,
    private val pushService: PushService,
    private val rateLimiter: PingRateLimiter,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun ping(
        authentication: Authentication,
        @Valid @RequestBody request: PingRequest,
    ) {
        val from = authentication.name

        if (!rateLimiter.allow(from, request.to)) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "chill out")
        }

        val payload = objectMapper.writeValueAsString(
        mapOf("type" to "ping", "from" to from)
        )
        webSocketHandler.deliver(request.to, payload)

        // Always also push - The socket may have died the last check
        pushService.notifyPing(toUsername = request.to, fromUsername = from)
    }
}
