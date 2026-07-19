package com.ltslr.gibbi.keys

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class OneTimePreKeyDto(
    val keyId: Int,
    @field:NotBlank
    val publicKey: String,
)

data class UploadKeysRequest(
    val registrationId: Int,

    @field:NotBlank
    val identityKey: String,

    val signedPreKeyId: Int,

    @field:NotBlank
    val signedPreKey: String,

    @field:NotBlank
    val signedPreKeySignature: String,

    @field:NotEmpty
    val oneTimePreKeys: List<OneTimePreKeyDto>,

    @field:NotBlank
    val kyberPreKeyId: Int,

    @field:NotBlank
    val kyberPreKey: String,

    @field:NotBlank
    val kyberPreKeySignature: String,
)

data class PreKeyBundleResponse(
    val registrationId: Int,
    val identityKey: String,
    val signedPreKeyId: Int,
    val signedPreKey: String,
    val signedPreKeySignature: String,
    val oneTimePreKeyId: Int?,
    val oneTimePreKey: String?,
    val kyberPreKeyId: Int,
    val kyberPreKey: String,
    val kyberPreKeySignature: String,
)

data class PreKeyCountResponse(
    val count: Long,
)
