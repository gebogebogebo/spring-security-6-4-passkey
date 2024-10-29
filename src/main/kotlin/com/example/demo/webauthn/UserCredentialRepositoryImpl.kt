package com.example.demo.webauthn

import org.springframework.security.web.webauthn.api.Bytes
import org.springframework.security.web.webauthn.api.CredentialRecord
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord
import org.springframework.security.web.webauthn.management.UserCredentialRepository
import org.springframework.stereotype.Component

@Component
class UserCredentialRepositoryImpl: UserCredentialRepository {
    var credentialRecords: ImmutableCredentialRecord? = null

    override fun save(credentialRecord: CredentialRecord) {
        val credentialId = credentialRecord.credentialId
        val userEntityUserId = credentialRecord.userEntityUserId
        val attestationClientDataJSON = credentialRecord.attestationClientDataJSON
        val attestationObject = credentialRecord.attestationObject

        credentialRecords = ImmutableCredentialRecord.builder()
            .credentialId(credentialId)
            .userEntityUserId(userEntityUserId)
            .attestationClientDataJSON(attestationClientDataJSON)
            .attestationObject(attestationObject)
            .build()
    }

    override fun findByCredentialId(credentialId: Bytes): CredentialRecord? {
        return credentialRecords
    }

    override fun findByUserId(userId: Bytes): List<CredentialRecord> {
        return if (credentialRecords != null) {
            listOf(credentialRecords!!)
        } else {
            emptyList()
        }
    }

    override fun delete(credentialId: Bytes) {
        // NOP
    }
}
