# gibbi relay — API contract

The authoritative wire contract between `Gibbi-Backend` (Spring Boot relay) and the gibbi KMP client. **Two repos, hand-synced.** Change this document in the same commit that changes either side.

Base URL (local dev): `http://localhost:8080`
Base URL (Android emulator): `http://10.0.2.2:8080`

## Conventions

- All request/response bodies are JSON.
- All endpoints except `/auth/**`, `/actuator/health`, `/error`, and the `/realtime` handshake require `Authorization: Bearer <jwt>`.
- **The caller's identity always comes from the JWT, never from a request field.** There is no `from` parameter anywhere — it cannot be forged.
- Ciphertext crosses the wire as **base64**. The server decodes it only to store `bytea` and re-encodes on the way out. It is never interpreted.
- Timestamps are ISO-8601 instants, UTC (e.g. `2026-07-19T22:04:03.758277100Z`).

---

## Auth

### `POST /auth/register`
```json
{ "username": "alice", "password": "password123" }
```
→ `200`
```json
{ "token": "eyJhbGciOiJIUzUxMiJ9...", "username": "alice" }
```

### `POST /auth/login`
Same request and response shape as register.

JWT: HMAC-SHA512, subject = username, 24h expiry (`jwt.expiration-ms`).

### `GET /me`
→ `200` `{ "username": "alice" }`

---

## Key directory

### `POST /keys`
Uploads (replaces) the caller's key bundle: identity key, signed prekey, Kyber prekey, and a batch of one-time prekeys.

```json
{
  "registrationId": 12345,
  "identityKey": "<base64>",
  "signedPreKeyId": 1,
  "signedPreKey": "<base64>",
  "signedPreKeySignature": "<base64>",
  "oneTimePreKeys": [
    { "keyId": 1, "publicKey": "<base64>" },
    { "keyId": 2, "publicKey": "<base64>" }
  ],
  "kyberPreKeyId": 1,
  "kyberPreKey": "<base64, ~1.5-2KB — column is length=4000>",
  "kyberPreKeySignature": "<base64>"
}
```
→ `204 No Content`

All string fields are base64-encoded key material; the server stores and returns them opaquely, same as ciphertext elsewhere in this contract. Call this once at registration/first-launch, and again whenever `GET /keys/count` reports the one-time prekey supply is running low (replenishment — not yet implemented client-side).

**Known server-side quirk:** `kyberPreKeyId` is annotated `@NotBlank` in `UploadKeysRequest`, which is a no-op on an `Int` — it does not actually validate this field. Not client-relevant, but don't rely on the server rejecting a missing/zero value here.

### `GET /keys/count`
→ `200`
```json
{ "count": 17 }
```
Returns the caller's own remaining one-time prekey count (server reads identity from the JWT — no username param). Useasdfa this to deccasdfide when to call `POST /keys` again with a fresh batch.

### `GET /keys/{username}`
Fetches a prekey bundle for starting a session with `{username}`. **Consume-on-fetch**: the returned one-time prekey (if any) is deleted server-side atomically with this read, so it is never handed out twice.

→ `200`
```json
{
  "registrationId": 12345,
  "identityKey": "<base64>",
  "signedPreKeyId": 1,
  "signedPreKey": "<base64>",
  "signedPreKeySignature": "<base64>",
  "oneTimePreKeyId": 3,
  "oneTimePreKey": "<base64>",
  "kyberPreKeyId": 1,
  "kyberPreKey": "<base64>",
  "kyberPreKeySignature": "<base64>"
}
```

`oneTimePreKeyId` / `oneTimePreKey` are **nullable** — if the target user's one-time prekeys are exhausted, PQXDH can still proceed using just the signed prekey + Kyber prekey (weaker forward secrecy for that session, but not blocked). The client must handle the null case rather than assume a one-time key is always present.

This endpoint is unauthenticated at the route level (no `Authentication` param in the controller) — but recall `SecurityConfig`'s `anyRequest → authenticated` rule still applies, so a valid Bearer token is required to reach it; the *recipient* username just comes from the path, not the caller's identity.

---

## Messaging

### `POST /messages`
```json
{ "to": "bob", "ciphertext": "<base64>", "type": 3 }
```
→ `202 Accepted`, empty body.

`type` is the libsignal message type (distinguishes a PreKeySignalMessage carrying the PQXDH handshake from a regular SignalMessage). It is routing metadata, not content.

**202 means accepted, not delivered.** The server does not reveal which path it took:
- Recipient has a live authenticated WebSocket → envelope pushed down it, nothing persisted.
- Otherwise → stored in the recipient's mailbox + `PushService` wake fired.

### `GET /messages`
Drains the caller's mailbox. **Consume-on-fetch**: rows are deleted in the same transaction as the read, so an envelope is never returned twice.

→ `200`
```json
[
  {
    "from": "alice",
    "ciphertext": "<base64>",
    "type": 3,
    "createdAt": "2026-07-19T22:04:03.758277100Z"
  }
]
```
Empty mailbox → `[]`.

### `POST /ping`
```json
{ "to": "bob" }
```
→ `202 Accepted`, empty body.
→ `429 Too Many Requests` if the same `(from, to)` pair pinged within the last 3 seconds.

A ping carries no content and no payload — only "A pinged B", which is metadata the relay already sees. It is therefore **not encrypted** and **not persisted**; a stale ping is worse than a dropped one.

**Both channels always fire.** Unlike `/messages`, the WebSocket result is ignored and the push is sent regardless — the socket may have died between the liveness check and the write. Clients must dedupe.

### `POST /push/register`
```json
{ "token": "<fcm-registration-token>", "platform": "android" }
```
→ `204 No Content`

Upsert by `(username, token)`: re-registering the same token updates `updated_at` rather than inserting a duplicate. A user may have multiple tokens (phone + tablet); all are valid targets.

---

## WebSocket — `/realtime`

`ws://localhost:8080/realtime` (`wss://` in production).

The handshake itself is **permitted without authentication** by Spring Security. Authentication happens in the protocol, not the filter chain.

### First-frame auth

The socket opens unauthenticated and can do nothing: it is not in the session registry, cannot receive deliveries, and is closed after **5 seconds** if it has not authenticated.

Client → server, must be the first frame:
```json
{ "type": "auth", "token": "eyJhbGciOiJIUzUxMiJ9..." }
```

Server → client on success:
```json
{ "type": "auth_ok" }
```

Close codes:

| Code | Reason | Cause |
|------|--------|-------|
| 1008 | `auth timeout` | No auth frame within 5s |
| 1008 | `auth failed` | Well-formed frame, but wrong `type` or invalid token |
| 1007 | `malform auth` | Payload was not parseable JSON |

One socket per username. A second login replaces the first (multi-device is out of scope for v1).

### Server → client frames

Message delivery:
```json
{
  "type": "message",
  "envelope": {
    "from": "alice",
    "ciphertext": "<base64>",
    "type": 3,
    "createdAt": "2026-07-19T22:04:03.758277100Z"
  }
}
```
The `envelope` object is identical in shape to an element of the `GET /messages` array — deliberately, so the client parses one type either way.

Ping delivery:
```json
{ "type": "ping", "from": "alice" }
```

### Client → server frames (post-auth)

None yet. Frames from an authenticated socket are currently ignored.

---

## Trust model

The server stores and relays opaque ciphertext and routing metadata. It never holds decryption keys and never sees plaintext — the `ciphertext` column is `bytea` and there is nowhere for plaintext to live.

**Honest limitation:** the relay necessarily learns *who talks to whom and when*, because it routes envelopes and sends pushes. It learns nothing about content. Push notifications carry only usernames — the `PushService` interface takes no payload parameter, so content cannot cross the push channel even by mistake.

---

## Open items

- Key directory DTOs (above) still need transcribing from source.
- `POST /push/register` collects tokens, but `PushService` is currently `NoOpPushService` (logs only). Real FCM slots in behind the same interface with no change to `MessageController` or `PingController`.
- Rate limiter and session registry are in-memory: single-instance only. Redis needed before horizontal scaling.
