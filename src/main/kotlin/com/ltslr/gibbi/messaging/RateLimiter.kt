package com.ltslr.gibbi.messaging

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class PingRateLimiter {
    private val lastPing = ConcurrentHashMap<String, Instant>()
    private val minInterval = Duration.ofSeconds(3)

    // Returns true if this ping is allowed, if its false, the pair is still cooling down
    fun allow(from: String, to: String): Boolean {
        val key = "$from->$to"
        val now = Instant.now()
        val previous = lastPing.put(key, now)
        if (previous != null && Duration.between(previous, now) < minInterval) {
            lastPing[key] = previous // Not leeting spams extend the window
            return false
        }
        return true
    }
}
