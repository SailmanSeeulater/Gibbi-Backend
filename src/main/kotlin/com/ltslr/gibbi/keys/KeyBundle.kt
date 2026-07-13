package com.ltslr.shma.keys

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "key_bundles")
class KeyBundle(

    @Id
    @Column(name = "owner_username")
    var ownerUsername: String,

    @Column(name = "registration_id", nullable = false)
    var registrationId: Int,

    @Column(name = "identity_key", nullable = false, length = 1000)
    var identityKey: String,

    @Column(name = "signed_prekey_id", nullable = false)
    var signedPreKeyId: Int,

    @Column(name = "signed_prekey", nullable = false, length = 1000)
    var signedPreKey: String,

    @Column(name = "signed_prekey_signature", nullable = false, length = 1000)
    var signedPreKeySignature: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "kyber_prekey_id", nullable = false)
    var kyberPreKeyId: Int,

    // Kyber keys are big, in base64, its aout ~2100 characters
    @Column(name = "kyber_prekey", nullable = false, length = 4000)
    var kyberPreKey: String,

    @Column(name = "kyber_prekey_signature", nullable = false, length = 1000)
    var kyberPreKeySignature: String,
)
