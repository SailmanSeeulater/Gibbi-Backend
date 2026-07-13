package com.ltslr.shma.config

import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): Map<String, Any> {
        val errors = ex.bindingResult.allErrors.map { error ->
            val field = (error as? FieldError)?.field ?: "unknown"
            val message = error.defaultMessage ?: "invalid"
            "$field: $message"
        }
        return mapOf(
            "status" to 400,
            "errors" to errors
        )
    }
}
