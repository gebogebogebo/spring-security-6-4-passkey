package com.example.demo.webauthn

import com.example.demo.repository.MpasskeyCredential
import com.example.demo.repository.MpasskeyCredentialRepository
import org.springframework.security.web.webauthn.api.Bytes
import org.springframework.security.web.webauthn.api.CredentialRecord
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose
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
        val publicKey = credentialRecord.publicKey
        val attestationClientDataJSON = credentialRecord.attestationClientDataJSON
        val attestationObject = credentialRecord.attestationObject

        // TODO Transports always seem to be empty.
        // val transports = credentialRecord.transports

        // TODO Better save it.
        // - signatureCount
        // - backupEligible
        // - created
        // - lastUsed

        val entity = MpasskeyCredential(
            id,
            credentialId.bytes,
            userInternalId,
            publicKey.bytes,
            attestationClientDataJSON.bytes,
            attestationObject.bytes,
        )

        // upsert entity
        mPasskeyCredentialRepository.save(entity)
    }

    override fun findByCredentialId(credentialId: Bytes): CredentialRecord? {
        return mPasskeyCredentialRepository.findByCredentialId(credentialId.bytes)?.let {
            toCredentialRecord(it)
        }
    }

    override fun findByUserId(userId: Bytes?): List<CredentialRecord> {
        val userInternalId = UserEntityIdUtil.toInternalId(userId) ?: return emptyList()
        val credentials = mPasskeyCredentialRepository.findByUserInternalId(userInternalId)

        return credentials.map { toCredentialRecord(it) }
    }

    private fun toCredentialRecord(entity: MpasskeyCredential): CredentialRecord {
        return ImmutableCredentialRecord.builder()
            .credentialId(Bytes(entity.credentialId))
            .userEntityUserId(UserEntityIdUtil.fromInternalId(entity.userInternalId))
            .publicKey(ImmutablePublicKeyCose(entity.publicKey))
            .attestationClientDataJSON(Bytes(entity.attestedCredentialDataJson))
            .attestationObject(Bytes(entity.attestationObject))
            .transports(emptySet())
            .build()
    }


    override fun delete(credentialId: Bytes) {
        TODO("Not yet implemented")
    }
}
