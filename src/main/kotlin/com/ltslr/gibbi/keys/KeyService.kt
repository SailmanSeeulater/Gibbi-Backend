package com.ltslr.shma.keys

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class KeyService(
    private val keyBundleRepository: KeyBundleRepository,
    private val oneTimePreKeyRepository: OneTimePreKeyRepository,
) {

    @Transactional
    fun uploadKeys(username: String, request: UploadKeysRequest) {
        val bundle = KeyBundle(
            ownerUsername = username,
            registrationId = request.registrationId,
            identityKey = request.identityKey,
            signedPreKeyId = request.signedPreKeyId,
            signedPreKey = request.signedPreKey,
            signedPreKeySignature = request.signedPreKeySignature,
            kyberPreKeyId = request.kyberPreKeyId,
            kyberPreKey = request.kyberPreKey,
            kyberPreKeySignature = request.kyberPreKeySignature,
        )
        keyBundleRepository.save(bundle)

        oneTimePreKeyRepository.deleteAllByOwnerUsername(username)
        val prekeys = request.oneTimePreKeys.map { dto ->
            OneTimePreKey(
                ownerUsername = username,
                keyId = dto.keyId,
                publicKey = dto.publicKey,
            )
        }
        oneTimePreKeyRepository.saveAll(prekeys)
    }

    @Transactional
    fun fetchBundle(username: String): PreKeyBundleResponse {
        val bundle = keyBundleRepository.findByOwnerUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No keys for user")

        val oneTime = oneTimePreKeyRepository.findFirstByOwnerUsername(username)
        if (oneTime != null) {
            oneTimePreKeyRepository.delete(oneTime)
        }

        return PreKeyBundleResponse(
            registrationId = bundle.registrationId,
            identityKey = bundle.identityKey,
            signedPreKeyId = bundle.signedPreKeyId,
            signedPreKey = bundle.signedPreKey,
            signedPreKeySignature = bundle.signedPreKeySignature,
            oneTimePreKeyId = oneTime?.keyId,
            oneTimePreKey = oneTime?.publicKey,
            kyberPreKeyId = bundle.kyberPreKeyId,
            kyberPreKey = bundle.kyberPreKey,
            kyberPreKeySignature = bundle.kyberPreKeySignature,
        )
    }

    fun countPreKeys(username: String): Long =
        oneTimePreKeyRepository.countByOwnerUsername(username)
}
