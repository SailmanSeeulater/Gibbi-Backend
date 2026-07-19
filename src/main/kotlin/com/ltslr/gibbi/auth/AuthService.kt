package com.ltslr.gibbi.auth

import com.ltslr.gibbi.user.User
import com.ltslr.gibbi.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username already taken")
        }
        val user = User(
            username = request.username,
            passwordHash = requireNotNull(passwordEncoder.encode(request.password)) {
                "Password encoder returned null"
            },
        )
        userRepository.save(user)
        val token = jwtService.generateToken(user.username)
        return AuthResponse(token = token, username = user.username)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash ?: "")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        val token = jwtService.generateToken(user.username)
        return AuthResponse(token = token, username = user.username)
    }
}
