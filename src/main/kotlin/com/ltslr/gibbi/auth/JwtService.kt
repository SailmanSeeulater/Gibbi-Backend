package com.ltslr.gibbi.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun isValid(token: String): Boolean =
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
}
