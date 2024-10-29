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
    var credentialRecords: ImmutableCredentialRecord? = null

    override fun save(credentialRecord: CredentialRecord) {
        /*
create table M_PASSKEY_CREDENTIAL (
    ID int default 0 not null auto_increment primary key,
    CREDENTIAL_ID varbinary not null unique,
    USER_INTERNAL_ID varchar not null,
    ATTESTED_CREDENTIAL_DATA_JSON varbinary,
    ATTESTATION_OBJECT varbinary
);
         */

        val credentialId = credentialRecord.credentialId
        val userInternalId = String(credentialRecord.userEntityUserId.bytes)
        val attestationClientDataJSON = credentialRecord.attestationClientDataJSON
        val attestationObject = credentialRecord.attestationObject

        val entity = MpasskeyCredential(
            0,
            credentialId.bytes,
            userInternalId,
            attestationClientDataJSON.bytes,
            attestationObject.bytes,
        )

        mPasskeyCredentialRepository.save(entity)
    }

    override fun findByCredentialId(credentialId: Bytes): CredentialRecord? {
        return credentialRecords
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
