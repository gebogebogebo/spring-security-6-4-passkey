package com.example.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                it.requestMatchers("/login").permitAll()
                it.anyRequest().authenticated()
            }
            .formLogin {
                it.loginPage("/login").permitAll()
                it.defaultSuccessUrl("/mypage")
            }
            .webAuthn {
                it.rpName("Spring Security Relying Party")
                it.rpId("localhost")
                it.allowedOrigins(setOf("http://localhost:8080"))
            }

        return http.build()
    }

}
