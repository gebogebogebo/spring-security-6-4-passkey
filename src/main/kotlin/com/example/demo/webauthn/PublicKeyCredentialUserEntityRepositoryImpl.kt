package com.example.demo.webauthn

import com.example.demo.repository.MuserRepository
import org.springframework.security.web.webauthn.api.Bytes
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository
import org.springframework.stereotype.Component

@Component
class PublicKeyCredentialUserEntityRepositoryImpl(
    private val mUserRepository: MuserRepository
) : PublicKeyCredentialUserEntityRepository {
    override fun findByUsername(username: String): PublicKeyCredentialUserEntity? {
        return mUserRepository.findByUserId(username)?.let {
            ImmutablePublicKeyCredentialUserEntity.builder()
                .id(createUserId(it.internalId))
                .name(it.userId)
                .displayName(it.displayName)
                .build()
        }
    }

    // TODO 共通化
    private fun createUserId(userId: String): Bytes {
        return Bytes(userId.toByteArray())
    }

    override fun findById(id: Bytes?): PublicKeyCredentialUserEntity {
        TODO("Not yet implemented")
    }

    override fun save(userEntity: PublicKeyCredentialUserEntity) {
        TODO("Not yet implemented")
    }

    override fun delete(id: Bytes?) {
        TODO("Not yet implemented")
    }

}
