package com.ltslr.gibbi.messaging

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MailboxRepository : JpaRepository<MailboxEntry, UUID> {
    fun findByToUsernameOrderByCreatedAtAsc(toUsername: String): List<MailboxEntry>

    @Modifying
    @Transactional
    @Query("DELETE FROM MailboxEntry m WHERE m.id IN :ids")
    fun deleteByIds(@Param("ids") ids: List<UUID>)
}
