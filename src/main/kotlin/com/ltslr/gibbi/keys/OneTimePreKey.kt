package com.ltslr.shma.keys

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "one_time_prekeys")
class OneTimePreKey(

    @Column(name = "owner_username", nullable = false)
    var ownerUsername: String,

    @Column(name = "key_id", nullable = false)
    var keyId: Int,

    @Column(name = "public_key", nullable = false, length = 1000)
    var publicKey: String,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
)
