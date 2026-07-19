package com.ltslr.gibbi.keys

import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/keys")
class KeyController(
    private val keyService: KeyService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun uploadKeys(
        authentication: Authentication,
        @Valid @RequestBody request: UploadKeysRequest,
    ) {
        keyService.uploadKeys(authentication.name, request)
    }

    @GetMapping("/count")
    fun countPreKeys(authentication: Authentication): PreKeyCountResponse =
        PreKeyCountResponse(keyService.countPreKeys(authentication.name))

    @GetMapping("/{username}")
    fun fetchBundle(@PathVariable username: String): PreKeyBundleResponse =
        keyService.fetchBundle(username)
}
