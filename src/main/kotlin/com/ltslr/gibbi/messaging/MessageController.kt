package com.ltslr.gibbi.messaging

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.ObjectMapper
import java.util.Base64
import java.time.Instant

@RestController
@RequestMapping("/messages")
class MessageController(
    private val mailboxService: MailboxService,
    private val pushService: PushService,
    private val webSocketHandler: GibbiWebSocketHandler,
    private val objectMapper: ObjectMapper,
) {    
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun send(
        authentication: Authentication,
        @Valid @RequestBody request: SendMessageRequest,
    ) {
        val from = authentication.name

        val envelope = MessageEnvelope(
            from = from,
            ciphertext = request.ciphertext,
            type = request.type,
            createdAt = Instant.now(),
        )
        val payload = objectMapper.writeValueAsString(
            mapOf("type" to "message", "envelope" to envelope)
        )

        if (webSocketHandler.deliver(request.to, payload)) return

        //Offline: Store and forward + wake the device
        mailboxService.store(
            toUsername = request.to,
            fromUsername = from,
            ciphertext = Base64.getDecoder().decode(request.ciphertext),
            messageType = request.type,
        )
        pushService.notifyNewMessage(toUsername = request.to, fromUsername = from)
    }

    @GetMapping
    fun drain(authentication: Authentication): List<MessageEnvelope> =
        mailboxService.drain(authentication.name).map { entry ->
            MessageEnvelope(
                from = entry.fromUsername,
                ciphertext = Base64.getEncoder().encodeToString(entry.ciphertext),
                type = entry.messageType,
                createdAt = entry.createdAt,
            )
        }
}
