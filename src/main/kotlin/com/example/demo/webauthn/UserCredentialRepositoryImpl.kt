package com.example.demo.webauthn

import org.springframework.security.web.webauthn.api.Bytes
import org.springframework.security.web.webauthn.api.CredentialRecord
import org.springframework.security.web.webauthn.management.UserCredentialRepository
import org.springframework.stereotype.Component

@Component
class UserCredentialRepositoryImpl: UserCredentialRepository {
    override fun delete(credentialId: Bytes) {
        // NOP
    }

    override fun save(credentialRecord: CredentialRecord) {
        // NOP
        val a = 0
    }

    override fun findByCredentialId(credentialId: Bytes): CredentialRecord? {
        // NPT
        return null
    }

    override fun findByUserId(userId: Bytes): List<CredentialRecord> {
        // NPT
        return emptyList()
    }

}