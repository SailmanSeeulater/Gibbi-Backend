package com.ltslr.gibbi.messaging

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoOpPushService : PushService {
    private val log = LoggerFactory.getLogger(NoOpPushService::class.java)

    override fun notifyNewMessage(toUsername: String, fromUsername: String) {
        log.info("[PUSH stub] would wake {} for new message from {}", toUsername, fromUsername)
    }

    override fun notifyPing(toUsername: String, fromUsername: String) {
        log.info("[PUSH stub] would ping {} from {}", toUsername, fromUsername)
    }
}
