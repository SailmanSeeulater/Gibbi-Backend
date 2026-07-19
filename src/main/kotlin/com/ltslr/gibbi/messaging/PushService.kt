package com.ltslr.gibbi.messaging

interface PushService {
    // Wake a device so it fetches its mailbox. No message content ever crosses the push channel
    fun notifyNewMessage(toUsername: String, fromUsername: String)

    // Deliver a ping wake. Sender handle only - no payload
    fun notifyPing(toUsername: String, fromUsername: String)
}
