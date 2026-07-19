package com.ltslr.gibbi.messaging

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PushTokenRepository : JpaRepository<PushToken, UUID> {
    fun findByUsername(username: String): List<PushToken>
    fun findByUsernameAndToken(username: String, token: String): PushToken?
}
