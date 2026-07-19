package com.ltslr.gibbi.messaging

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MailboxService(
    private val mailboxRepository: MailboxRepository,
) {
    fun store(toUsername: String, fromUsername: String, ciphertext: ByteArray, messageType: Int) {
        mailboxRepository.save(
            MailboxEntry(
                toUsername = toUsername,
                fromUsername = fromUsername,
                ciphertext = ciphertext,
                messageType = messageType,
            )
        )
    }

    // Consume-on-fetch: read the caller's evelops and delete them in the same transaction.
    @Transactional
    fun drain(username: String): List<MailboxEntry> {
        val entries = mailboxRepository.findByToUsernameOrderByCreatedAtAsc(username)
        if (entries.isNotEmpty()) {
            mailboxRepository.deleteByIds(entries.mapNotNull { it.id })
        }
        return entries
    }
}
