package com.ltslr.gibbi.messaging

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "push_tokens")
class PushToken(
    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "token", nullable = false, length = 512)
    var token: String,

    @Column(name ="platform", nullable = false)
    var platform: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
)
