package com.example.demo.service

import com.example.demo.repository.MuserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class SampleUserDetailsService(
    private val mUserRepository: MuserRepository
): UserDetailsService {
    override fun loadUserByUsername(userId: String?): UserDetails {
        if (userId.isNullOrEmpty()) {
            throw UsernameNotFoundException("userId is null or empty")
        }

        val mUser = mUserRepository.findById(userId).orElse(null) ?: throw UsernameNotFoundException("Not found userId")

        return User(mUser.id,mUser.password, Collections.emptyList())
    }
}
