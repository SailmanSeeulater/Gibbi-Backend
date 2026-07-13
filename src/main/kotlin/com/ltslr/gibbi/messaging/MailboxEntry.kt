package com.ltslr.shma.messaging

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "mailbox")
class MailboxEntry(
    @Column(name = "to_username", nullable = false)
    var toUsername: String,

    @Column(name = "from_username", nullable = false)
    var fromUsername: String,

    @Column(name = "ciphertext", nullable = false, columnDefinition = "bytea")
    var ciphertext: ByteArray,

    @Column(name = "message_type", nullable = false)
    var messageType: Int,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
)
