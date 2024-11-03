package com.example.demo.webauthn

import org.springframework.security.web.webauthn.api.Bytes

class UserEntityIdUtil {
    companion object {
        fun fromInternalId(internalId: String): Bytes {
            return Bytes(internalId.toByteArray())
        }

        fun toInternalId(userEntityId: Bytes?): String? {
            return userEntityId?.bytes?.let { String(it) }
        }
    }
}
