package com.example.demo.webauthn

import com.example.demo.repository.Muser
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
    override fun findByUsername(username: String): PublicKeyCredentialUserEntity {
        if (username == "anonymousUser") {
            return ImmutablePublicKeyCredentialUserEntity.builder().build()
        }

        val user = mUserRepository.findByUserId(username)
            ?: return ImmutablePublicKeyCredentialUserEntity.builder().build()

        return toPublicKeyCredentialUserEntity(user)
    }

    override fun findById(id: Bytes): PublicKeyCredentialUserEntity? {
        val userInternalId = UserEntityIdUtil.toInternalId(id) ?: return null

        return mUserRepository.findByInternalId(userInternalId)?.let {
            toPublicKeyCredentialUserEntity(it)
        }
    }

    private fun toPublicKeyCredentialUserEntity(user: Muser): PublicKeyCredentialUserEntity {
        return ImmutablePublicKeyCredentialUserEntity.builder()
            .id(UserEntityIdUtil.fromInternalId(user.internalId))
            .name(user.userId)
            .displayName(user.displayName)
            .build()
    }


    override fun save(userEntity: PublicKeyCredentialUserEntity) {
        TODO("Not yet implemented")
    }

    override fun delete(id: Bytes?) {
        TODO("Not yet implemented")
    }
}
