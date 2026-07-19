package com.ltslr.gibbi.messaging

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class RegisterPushTokenRequest(
    @field:NotBlank val token: String,
    @field:NotBlank val platform: String,
)

@RestController
@RequestMapping("/push")
class PushController(
    private val pushTokenRepository: PushTokenRepository,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun register(
        authentication: Authentication,
        @Valid @RequestBody request: RegisterPushTokenRequest,
    ) {
        val username = authentication.name
        val existing = pushTokenRepository.findByUsernameAndToken(username, request.token)
        if (existing != null) {
            existing.updatedAt = Instant.now()
            pushTokenRepository.save(existing)
            return
        }
        pushTokenRepository.save(
            PushToken(username = username, token = request.token, platform = request.platform)
        )
    }
}
