package com.ltslr.shma.keys

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OneTimePreKeyRepository : JpaRepository<OneTimePreKey, java.util.UUID> {

    fun findFirstByOwnerUsername(ownerUsername: String): OneTimePreKey?

    fun countByOwnerUsername(ownerUsername: String): Long

    @Modifying
    @Query("DELETE FROM OneTimePreKey k WHERE k.ownerUsername = :owner")
    fun deleteAllByOwnerUsername(@Param("owner") owner: String)
}
