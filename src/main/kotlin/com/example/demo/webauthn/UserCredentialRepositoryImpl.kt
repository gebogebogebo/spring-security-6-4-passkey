package com.example.demo.webauthn

import com.example.demo.repository.MpasskeyCredential
import com.example.demo.repository.MpasskeyCredentialRepository
import org.springframework.security.web.webauthn.api.Bytes
import org.springframework.security.web.webauthn.api.CredentialRecord
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord
import org.springframework.security.web.webauthn.management.UserCredentialRepository
import org.springframework.stereotype.Component

@Component
class UserCredentialRepositoryImpl(
    private val mPasskeyCredentialRepository: MpasskeyCredentialRepository,
) : UserCredentialRepository {
    override fun save(credentialRecord: CredentialRecord) {
        val rec = mPasskeyCredentialRepository.findByCredentialId(credentialRecord.credentialId.bytes)
        val id = rec?.id ?: 0

        val credentialId = credentialRecord.credentialId
        val userInternalId = String(credentialRecord.userEntityUserId.bytes)
        val attestationClientDataJSON = credentialRecord.attestationClientDataJSON
        val attestationObject = credentialRecord.attestationObject

        val entity = MpasskeyCredential(
            id,
            credentialId.bytes,
            userInternalId,
            attestationClientDataJSON.bytes,
            attestationObject.bytes,
        )

        mPasskeyCredentialRepository.save(entity)
    }

    override fun findByCredentialId(credentialId: Bytes): CredentialRecord? {
        return mPasskeyCredentialRepository.findByCredentialId(credentialId.bytes)?.let{
            ImmutableCredentialRecord.builder()
                .credentialId(Bytes(it.credentialId))
                .userEntityUserId(createUserId(it.userInternalId))
                .attestationClientDataJSON(Bytes(it.attestedCredentialDataJson))
                .attestationObject(Bytes(it.attestationObject))
                .build()
        }
    }

    override fun findByUserId(userId: Bytes): List<CredentialRecord> {
        // TODO 共通化
        val userInternalId = String(userId.bytes)

        val credentials = mPasskeyCredentialRepository.findByUserInternalId(userInternalId)

        return credentials.map {
            ImmutableCredentialRecord.builder()
                .credentialId(Bytes(it.credentialId))
                .userEntityUserId(createUserId(it.userInternalId))
                .attestationClientDataJSON(Bytes(it.attestedCredentialDataJson))
                .attestationObject(Bytes(it.attestationObject))
                .build()
        }
    }

    // TODO 共通化
    private fun createUserId(userId: String): Bytes {
        return Bytes(userId.toByteArray())
    }

    override fun delete(credentialId: Bytes) {
        // NOP
    }
}
