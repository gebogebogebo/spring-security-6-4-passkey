package com.example.demo.repository

import org.springframework.data.jpa.repository.JpaRepository

interface MpasskeyCredentialRepository : JpaRepository<MpasskeyCredential, Int> {
    fun findByUserInternalId(userInternalId: String): List<MpasskeyCredential>
}