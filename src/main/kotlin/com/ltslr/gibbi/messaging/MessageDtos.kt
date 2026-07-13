package com.ltslr.shma.messaging

import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class SendMessageRequest(
    @field:NotBlank val to: String,
    // Base64 encoding ciphertext. The server doesnt decode this
    @field:NotBlank val ciphertext: String,
    val type: Int,
)

data class MessageEnvelope(
    val from: String,
    val ciphertext: String,
    val type: Int,
    val createdAt: Instant,
)
