package com.ltslr.shma.messaging

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

@RestController
@RequestMapping("/messages")
class MessageController(
    private val mailboxService: MailboxService,
    private val pushService: PushService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun send(
        authentication: Authentication,
        @Valid @RequestBody request: SendMessageRequest,
    ) {
        val from = authentication.name
        val bytes = Base64.getDecoder().decode(request.ciphertext)

        // TODO(step 5): if recipient has a live WebSocket, deliver instantly instead.
        mailboxService.store(
            toUsername = request.to,
            fromUsername = from,
            ciphertext = bytes,
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
