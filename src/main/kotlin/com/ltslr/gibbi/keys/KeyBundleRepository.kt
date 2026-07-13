package com.ltslr.shma.keys

import org.springframework.data.jpa.repository.JpaRepository

interface KeyBundleRepository : JpaRepository<KeyBundle, String> {

    fun findByOwnerUsername(ownerUsername: String): KeyBundle?
}
